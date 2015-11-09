package nl.MakerTim.HubEssentials;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class BukkitListener implements Listener {

	private static final String[] SHOP_FORMAT = { "shop", "kost", "kopen", "whitelist", "vip" };
	private static final String[] BUGS_FORMAT = { "help", "bug", "fout", "error" };
	public static final Map<UUID, List<Location>> TP_HISTORY = new HashMap<>();
	public static final List<UUID> VANISH = new ArrayList<>();

	@EventHandler
	public void onJoin(AsyncPlayerPreLoginEvent apple) {
		if (!BukkitStarter.isDev(apple.getUniqueId()) && BukkitStarter.plugin.devMode) {
			apple.disallow(Result.KICK_FULL, "Server is now in dev mode.");
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onTP(PlayerTeleportEvent pte) {
		if (!pte.isCancelled()) {
			if (!TP_HISTORY.containsKey(pte.getPlayer().getUniqueId())) {
				TP_HISTORY.put(pte.getPlayer().getUniqueId(), new ArrayList<Location>());
			}
			TP_HISTORY.get(pte.getPlayer().getUniqueId()).add(pte.getFrom());
		}
	}

	public static void updateVanish() {
		List<Player> nonHidden = new ArrayList<Player>(Bukkit.getOnlinePlayers());
		for (Player pl : Bukkit.getOnlinePlayers()) {
			for (UUID vanished : VANISH) {
				Player vanish = Bukkit.getPlayer(vanished);
				if (vanish != null) {
					pl.hidePlayer(vanish);
					nonHidden.remove(vanish);
				}
			}
			for (Player nonVanish : nonHidden) {
				pl.showPlayer(nonVanish);
			}
		}
	}

	@EventHandler
	public void updateVanish(PlayerJoinEvent pje) {
		updateVanish();
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
			apce.getPlayer()
					.sendMessage(ChatColor.GOLD + ChatColor.BOLD.toString()
							+ "Do you like to purchase one of our fine VIP ranks or a whitelist? Click here "
							+ ChatColor.BLUE + ChatColor.UNDERLINE + "shop.iMine.nl");
		}
		sendMssg = false;
		for (String bgs : BUGS_FORMAT) {
			if (mssg.toLowerCase().contains(bgs)) {
				sendMssg = true;
			}
		}
		if (sendMssg) {
			apce.getPlayer().sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Found a bug? Report it to "
					+ ChatColor.BLUE + ChatColor.UNDERLINE + "bugs.iMine.nl");
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
