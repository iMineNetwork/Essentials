package nl.MakerTim.HubEssentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
		}
		return false;
	}
}
