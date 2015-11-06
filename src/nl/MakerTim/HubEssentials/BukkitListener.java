package nl.MakerTim.HubEssentials;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;

public class BukkitListener implements Listener {

	private static final String[] SHOP_FORMAT = { "shop", "duur", "kost", "kopen", "whitelist", "vip" };
	private static final String[] BUGS_FORMAT = { "help", "bug", "fout", "error" };

	@EventHandler
	public void onJoin(AsyncPlayerPreLoginEvent event) {
		if (!BukkitStarter.isDev(event.getUniqueId()) && BukkitStarter.plugin.devMode) {
			event.disallow(Result.KICK_FULL, "Server is now in dev mode.");
		}
	}

	@EventHandler
	public void joinDeop(PlayerJoinEvent pje) {
		if (pje.getPlayer().isOp()) {
			pje.getPlayer().setOp(false);
		}
	}

	@EventHandler
	public void chat(AsyncPlayerChatEvent apce) {
		String mssg = apce.getMessage();
		boolean sendMssg = false;
		for (String shp : SHOP_FORMAT) {
			if (mssg.toLowerCase().contains(shp)) {
				sendMssg = true;
			}
		}
		if (sendMssg) {
			apce.getPlayer().sendMessage(ChatColor.GOLD + ChatColor.BOLD.toString() + "Do you like to purchase one of our fine VIP ranks or a whitelist? Click here -> "
					+ ChatColor.BLUE + "shop.iMine.nl");
		}
		sendMssg = false;
		for (String bgs : BUGS_FORMAT) {
			if (mssg.toLowerCase().contains(bgs)) {
				sendMssg = true;
			}
		}
		if (sendMssg) {
			apce.getPlayer().sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Found a bug? Report it to -> "
					+ ChatColor.BLUE + "bugs.iMine.nl");
		}
	}

	@EventHandler
	public void onJoining(PlayerJoinEvent pje) {
		DatabaseManager db = BukkitStarter.plugin.getDB();
		db.insertQuery("DELETE FROM `iMine_Bans`.`AdminRegister` WHERE `AdminRegister`.`UUID` = '"
				+ pje.getPlayer().getUniqueId() + "';");
		if (pje.getPlayer().isOp() || BukkitStarter.isDev(pje.getPlayer().getUniqueId())) {
			pje.getPlayer().performCommand("git");
			db.insertQuery("INSERT INTO `iMine_Bans`.`AdminRegister` (`UUID`, `isAdmin`) VALUES ('"
					+ pje.getPlayer().getUniqueId() + "', '1');");
		}
	}
}
