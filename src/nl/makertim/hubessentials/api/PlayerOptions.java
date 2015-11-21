package nl.makertim.hubessentials.api;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;

public class PlayerOptions {

	public final Player pl;

	public PlayerOptions(final Player pl) {
		this.pl = pl;
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
                player.getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.TITLE, ChatSerializer.a("{\"text\": \"" + title + "\"}"), duratio, duratio, duratio));
            }
            if (subTitle != null) {
                player.getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, ChatSerializer.a("{\"text\": \"" + subTitle + "\"}")));
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
