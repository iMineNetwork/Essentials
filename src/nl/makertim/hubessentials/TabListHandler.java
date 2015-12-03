package nl.makertim.hubessentials;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import nl.makertim.hubessentials.api.ColorFormatter;

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

	private void updateAll() {
		for (Player pl : Bukkit.getOnlinePlayers()) {
			setTabTitle(pl);
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent pje) {
		setTabTitle(pje.getPlayer());
	}

	private void setTabTitle(Player pl) {
		try {
			net.minecraft.server.v1_8_R3.IChatBaseComponent tabTitle = net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer
					.a("{\"text\": \"" + top + "\"}");
			net.minecraft.server.v1_8_R3.IChatBaseComponent tabFoot = net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer
					.a("{\"text\": \"" + bottom + "\"}");
			net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter packet = new net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter();
			Field headerMethod = packet.getClass().getDeclaredField("a");
			headerMethod.setAccessible(true);
			headerMethod.set(packet, tabTitle);
			headerMethod.setAccessible(false);
			Field footerMethod = packet.getClass().getDeclaredField("b");
			footerMethod.setAccessible(true);
			footerMethod.set(packet, tabFoot);
			footerMethod.setAccessible(false);
			((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) pl).getHandle().playerConnection.sendPacket(packet);
		} catch (Throwable th) {
		}
	}
}
