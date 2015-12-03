package nl.makertim.hubessentials;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import nl.makertim.hubessentials.api.ColorFormatter;
import nl.makertim.hubessentials.api.PlayerOptions;

public class TabListHandler implements Listener {

	private String top, bottom;

	public TabListHandler() {
		updateTitle();
		Bukkit.getPluginManager().registerEvents(this, BukkitStarter.plugin);
	}

	private void updateTitle() {
		FileConfiguration config = BukkitStarter.plugin.getConfig();
		if (config.getString("tab.top") == null) {
			config.set("tab.top", "&6&liMineNetwerk");
			BukkitStarter.plugin.saveConfig();
		}
		if (config.getString("tab.bottom") == null) {
			config.set("tab.bottom", "&f&liMine.nl");
			BukkitStarter.plugin.saveConfig();
		}
		top = ColorFormatter.replaceColors(config.getString("tab.top"));
		bottom = ColorFormatter.replaceColors(config.getString("tab.bottom"));
	}

	public void updateTop(String newTop) {
		BukkitStarter.plugin.getConfig().set("tab.top", newTop);
		BukkitStarter.plugin.saveConfig();
		top = newTop;
		updateAll();
	}

	public void updateBottom(String newBottom) {
		BukkitStarter.plugin.getConfig().set("tab.bottom", newBottom);
		BukkitStarter.plugin.saveConfig();
		bottom = newBottom;
		updateAll();
	}

	public void updateAll() {
		for (Player pl : Bukkit.getOnlinePlayers()) {
			PlayerOptions po = new PlayerOptions(pl);
			po.updateTabPrefix();
			po.setTabTitle(top, bottom);
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent pje) {
		updateAll();
	}
}
