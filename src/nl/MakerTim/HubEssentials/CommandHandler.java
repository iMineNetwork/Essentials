package nl.MakerTim.HubEssentials;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CommandHandler {

	public static boolean onCommand(CommandSender sender, String command, String[] args) {
		if (command.equalsIgnoreCase("hub") && sender instanceof Player) {
			if (args.length == 0) {
				MktUtils.sendPlayerToServer(BukkitStarter.plugin, (Player) sender, "hub");
			} else if (sender.isOp() || sender.hasPermission("hub.op")) {
				MktUtils.sendPlayerToServer(BukkitStarter.plugin, (Player) sender, args[0]);
			} else {
				sender.sendMessage(ChatColor.RED + "You have no acces to this command");
			}
			return true;
		} else if (command.equalsIgnoreCase("dev")) {
			if (sender.isOp() || sender.hasPermission("iMine.dev")) {
				BukkitStarter.plugin.devMode = !BukkitStarter.plugin.devMode;
				if (BukkitStarter.plugin.devMode) {
					for (Player pl : Bukkit.getOnlinePlayers()) {
						if (!BukkitStarter.isDev(pl.getUniqueId())) {
							MktUtils.sendPlayerToServer(BukkitStarter.plugin, pl, "hub");
						}
					}
				}
			}
		} else if (command.equalsIgnoreCase("git")) {
			if (sender.isOp() || sender.hasPermission("iMine.dev")) {
				for (Plugin pl : Bukkit.getPluginManager().getPlugins()) {
					String version = pl.getDescription().getVersion();
					Pattern p = Pattern.compile("\\b([0-9a-f]{5,40})\\b");
					Matcher match = p.matcher(version);
					if (match.find()) {
						sender.sendMessage(pl.getName() + " - " + match.group(0));
						if (match.hitEnd()) {
							// Alleen short
						} else {
							// Short en long!
							String[] data = BukkitStarter.API.getProjectData(pl.getName());
							if (data != null) {
								for (String str : data) {
									sender.sendMessage(ChatColor.GOLD + str);
								}
							} else {
								sender.sendMessage(ChatColor.GOLD + "null");
							}
						}
					}
				}
			}
		}
		return false;
	}
}
