package nl.MakerTim.HubEssentials;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitStarter extends JavaPlugin {

	public static final GitLabAPI API = new GitLabAPI();
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
		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		Bukkit.getPluginManager().registerEvents(new BukkitListener(), this);
	}

	@Override
	public void onDisable() {
		plugin = null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return CommandHandler.onCommand(sender, command.getName(), args);
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
		return ret;
	}
}
