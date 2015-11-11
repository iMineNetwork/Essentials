package nl.makertim.hubessentials;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class MktUtils {
	private final static String[][] COLOR_CODES = new String[][] { { "&0", ChatColor.BLACK.toString() },
			{ "&1", ChatColor.DARK_BLUE.toString() }, { "&2", ChatColor.DARK_GREEN.toString() },
			{ "&3", ChatColor.DARK_AQUA.toString() }, { "&4", ChatColor.DARK_RED.toString() },
			{ "&5", ChatColor.DARK_PURPLE.toString() }, { "&6", ChatColor.GOLD.toString() },
			{ "&7", ChatColor.GRAY.toString() }, { "&8", ChatColor.DARK_GRAY.toString() },
			{ "&9", ChatColor.BLUE.toString() }, { "&a", ChatColor.GREEN.toString() },
			{ "&b", ChatColor.AQUA.toString() }, { "&c", ChatColor.RED.toString() },
			{ "&d", ChatColor.LIGHT_PURPLE.toString() }, { "&e", ChatColor.YELLOW.toString() },
			{ "&f", ChatColor.WHITE.toString() }, { "&k", ChatColor.MAGIC.toString() },
			{ "&l", ChatColor.BOLD.toString() }, { "&m", ChatColor.STRIKETHROUGH.toString() },
			{ "&n", ChatColor.UNDERLINE.toString() }, { "&o", ChatColor.ITALIC.toString() },
			{ "&r", ChatColor.RESET.toString() } };

	public static void sendPlayerToServer(final Plugin p, final Player pl, final String serverID) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeUTF("Connect");
			dos.writeUTF(serverID);
			pl.sendPluginMessage(p, "BungeeCord", baos.toByteArray());
			baos.close();
			dos.close();
		} catch (Exception ex) {
			System.err.println("Player " + pl.getName() + " has not been sended to " + serverID + " because of "
					+ ex.getMessage());
			pl.sendMessage("You " + " have not been sended to " + serverID + " because of " + ex.getMessage());
		}
	}

	public static String replaceColors(String toReplace) {
		if (toReplace == null) {
			return "";
		}
		for (String[] strs : COLOR_CODES) {
			toReplace = toReplace.replaceAll(strs[0], strs[1]);
		}
		return toReplace;
	}
}
