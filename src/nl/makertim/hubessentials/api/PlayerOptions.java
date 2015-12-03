package nl.makertim.hubessentials.api;

import java.lang.reflect.Field;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;

public class PlayerOptions {

	public final Player pl;

	public PlayerOptions(final Player pl) {
		this.pl = pl;
	}

	public String getPermissionPrefix() {
		String ret = "";
		if (pl.hasPermission("iMine.tab.SU")) {
			ret = "&c[SU]&r";
		} else if (pl.hasPermission("iMine.tab.OP")) {
			ret = "&c[OP]&r";
		} else if (pl.hasPermission("iMine.tab.HELPER")) {
			ret = "&2[H]&r";
		} else if (pl.hasPermission("iMine.tab.CHATMOD")) {
			ret = "&2[c]&r";
		} else if (pl.hasPermission("iMine.tab.VIP+")) {
			ret = "&6[+]&r";
		} else if (pl.hasPermission("iMine.tab.VIP")) {
			ret = "&7[+]&r";
		} else if (pl.hasPermission("iMine.tab.BUILDER")) {
			ret = "&7[b]&r";
		}
		return ColorFormatter.replaceColors(ret);
	}

	public void updateTabPrefix() {
		pl.setPlayerListName(getPermissionPrefix() + " " + pl.getDisplayName());
	}

	public void setTabTitle(String top, String bottom) {
		try {
			IChatBaseComponent tabTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + top + "\"}");
			IChatBaseComponent tabFoot = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + bottom + "\"}");
			PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
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

	public void sendActionMessage(String message) {
		try {
			IChatBaseComponent icbc = ChatSerializer.a("{\"text\": \"" + message + "\"}");
			PacketPlayOutChat ppoc = new PacketPlayOutChat(icbc, (byte) 2);
			((CraftPlayer) pl).getHandle().playerConnection.sendPacket(ppoc);
		} catch (Exception ex) {
			pl.sendMessage(message);
		}
	}

	public void sendTitleMessage(String title, String subTitle, int duratio) {
		try {
			CraftPlayer player = ((CraftPlayer) pl);
			if (title != null) {
				player.getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.TITLE,
						ChatSerializer.a("{\"text\": \"" + title + "\"}"), duratio, duratio, duratio));
			}
			if (subTitle != null) {
				player.getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.SUBTITLE,
						ChatSerializer.a("{\"text\": \"" + subTitle + "\"}")));
			}
		} catch (Exception ex) {
			if (title != null) {
				pl.sendMessage(title);
			}
			if (subTitle != null) {
				pl.sendMessage(subTitle);
			}
		}
	}
}
