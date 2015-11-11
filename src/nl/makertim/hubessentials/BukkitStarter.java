package nl.makertim.hubessentials;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitStarter extends JavaPlugin {

	public static final GitLabAPI API = new GitLabAPI();
	public static final File UPDATE_DIR = new File("plugins/update/");
	private static final Long PEX_DELAY = 20L * 60L * 13L; // Try to random
	private static final UUID[] DEVS = new UUID[] { UUID.fromString("650f464f-c81a-4050-a2bf-4daac8139873"), // MakerTim
			UUID.fromString("5451cfd7-ca8d-442c-81f9-81859bd9adc2"), // MKT
			UUID.fromString("b524c508-9f1e-4cef-965a-4a7b9017055c"), // Sansko1337
			UUID.fromString("9bbc05fa-f36c-401a-8c23-c57f5ba5c5ee"), // Sansko1338
			UUID.fromString("4697c45f-e9fe-4a0a-abeb-8783d6ac7ca6"), // Beauseant
			UUID.fromString("203fe4b3-928f-4780-8067-6d557fdcc153"), // wsslfnstr
	};
	public static BukkitStarter plugin;

	private DatabaseManager dm;
	public boolean devMode = false;

	@Override
	public void onEnable() {
		plugin = this;
		dm = new DatabaseManager(Credentials.getDatabase(), Credentials.getUsername(), Credentials.getPassword());
		setupConfig();
		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		Bukkit.getPluginManager().registerEvents(new BukkitListener(), this);
		Bukkit.getPluginManager().registerEvents(new Profiler(), this);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new PexReloader(), PEX_DELAY, PEX_DELAY);
	}

	@Override
	public void onDisable() {
		plugin = null;
		updatePlugins();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return new CommandHandler(sender, command.getName(), args).onCommand();
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (sender instanceof Player) {
			List<String> list = CommandHandler.onTabComplete((Player) sender, command.getName(), args);
			return list;
		}
		return super.onTabComplete(sender, command, alias, args);
	}

	private void setupConfig() {
		String check = "QWERTY" + Math.random();
		if (getConfig().getString("ServerName", check).equalsIgnoreCase(check)) {
			getConfig().set("ServerName", "Unknown");
			saveConfig();
		}
	}

	public void updatePlugins() {
		File[] directoryListing = UPDATE_DIR.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				File newFile = new File("plugins/" + child.getName());
				boolean del = newFile.delete();
				if (child.renameTo(newFile)) {
					System.out.println("[UPDATELOG] +" + child.getName());
				} else {
					System.out.println("[UPDATELOG] !" + child.getName() + " failed to update"
							+ (del ? "" : ", couldn't find old file!"));
				}
			}
		} else {
			System.out.println("plugins updatefolder not found!");
			UPDATE_DIR.mkdirs();
		}
	}

	public DatabaseManager getDB() {
		return dm;
	}

	public static boolean isDev(UUID check) {
		boolean ret = false;
		for (UUID dev : DEVS) {
			if (check.equals(dev)) {
				ret = true;
			}
		}
		if (!ret && Bukkit.getPlayer(check) != null && Bukkit.getPlayer(check).hasPermission("iMine.helper")) {
			ret = true;
		}
		return ret;
	}
}
