package nl.makertim.hubessentials.api;

import org.bukkit.ChatColor;

public class ColorFormatter {

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

	public static String rainbow(String toReplace) {
		if (toReplace == null) {
			return "";
		}
		String ret = "";
		int i = 10;
		for (char c : toReplace.toCharArray()) {
			ret += COLOR_CODES[i][1] + c;
			i = (i + 1) % COLOR_CODES.length;
			if (i == 0) {
				i = 10;
			}
		}
		return ret;
	}

	public static String replaceColors(String toReplace) {
		if (toReplace == null) {
			return "";
		}
		if (toReplace.startsWith("&z")) {
			return rainbow(toReplace.replaceFirst("&z", ""));
		}
		for (String[] strs : COLOR_CODES) {
			toReplace = toReplace.replaceAll(strs[0], strs[1]);
		}
		return toReplace;
	}
}
