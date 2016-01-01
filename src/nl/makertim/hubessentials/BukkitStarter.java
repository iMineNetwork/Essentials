package nl.makertim.hubessentials;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import nl.makertim.hubessentials.api.DatabaseManager;

public class BukkitStarter extends JavaPlugin {

	public static final GitLabAPI API = new GitLabAPI();
	public static final File UPDATE_DIR = new File("plugins/update/");
	private static final Long PEX_DELAY = 20L * 60L * 13L; // Try to random

	public static BukkitStarter plugin;

	private DatabaseManager dm;
	private TabListHandler tlh;
	public boolean devMode = false;

	@Override
	public void onEnable() {
		plugin = this;
		//new NewYear();
		dm = new DatabaseManager(Credentials.getDatabase(), Credentials.getUsername(), Credentials.getPassword());
		setupConfig();
		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		Bukkit.getPluginManager().registerEvents(new BukkitListener(), this);
		Bukkit.getPluginManager().registerEvents(new Profiler(), this);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new PexReloader(), PEX_DELAY, PEX_DELAY);
		tlh = new TabListHandler();
	}

	@Override
	public void onDisable() {
		plugin = null;
		System.out.println("Hub Essentials logg off");
		updatePlugins();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return new CommandHandler(sender, command.getName(), args).onCommand();
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (sender instanceof Player) {
			return CommandHandler.onTabComplete((Player) sender, command.getName(), args);
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

	public TabListHandler getTLH() {
		return tlh;
	}
}
