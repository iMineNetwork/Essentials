package nl.makertim.essentials;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import nl.imine.api.db.iMinePlayer;
import nl.imine.api.event.VanishUpdateEvent;
import nl.imine.api.util.ColorUtil;

public class BukkitListener implements Listener {

	private static final String[] SHOP_FORMAT = {"shop", "kost", "duur", "kopen", "whitelist", "vip"};
	private static final String[] BUGS_FORMAT = {"help", "bug", "fout", "error"};
	private static final String[] TS_FORMAT = {" ts", "teamspeak", "team", "speak"};
	private static final String[] REPORT_FORMAT = {"hax", "hack", "hex", "h@x", "h3x", "hAck", "h3ck", "flyh", "cheat",
			"aura"};
	private static final Set<UUID> MUTED = new HashSet<>();
	public static final Map<UUID, List<Location>> TP_HISTORY = new HashMap<>();

	public static boolean vanishAble = true;

	@EventHandler(priority = EventPriority.MONITOR)
	public void joinMessage(PlayerJoinEvent pje) {
		String str = pje.getJoinMessage();
		if (str != null && !str.trim().isEmpty()) {
			pje.setJoinMessage(null);
			Bukkit.getOnlinePlayers().stream().filter(opl -> opl.canSee(pje.getPlayer()))
					.forEach(opl -> opl.sendMessage(str));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void leaveMessage(PlayerQuitEvent pqe) {
		String str = pqe.getQuitMessage();
		if (str != null && !str.trim().isEmpty()) {
			pqe.setQuitMessage(null);
			Bukkit.getOnlinePlayers().stream().filter(opl -> opl.canSee(pqe.getPlayer()))
					.forEach(opl -> opl.sendMessage(str));
		}
	}

	@EventHandler
	public void onJoin(PlayerLoginEvent ple) {
		if (BukkitStarter.plugin.devMode && !ple.getPlayer().hasPermission("iMine.devOverride")) {
			ple.disallow(Result.KICK_FULL, "Server is now in dev mode.");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMinecraftCommand(PlayerCommandPreprocessEvent pcpe) {
		if (pcpe.getMessage().contains("minecraft:")
				&& !pcpe.getPlayer().hasPermission("iMine.command.minecraft.bypass")) {
			pcpe.setCancelled(true);
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

	public static void toggleMuted(OfflinePlayer pl) {
		if (isMuted(pl)) {
			MUTED.remove(pl.getUniqueId());
			if (pl.isOnline()) {
				((Player) pl).sendMessage("-Free to talk again-");
			}
		} else {
			MUTED.add(pl.getUniqueId());
			if (pl.isOnline()) {
				((Player) pl).sendMessage("-No talky talky for you-");
			}
		}
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent apce) {
		if (isMuted(apce.getPlayer())) {
			apce.setCancelled(true);
		}
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent pcpe) {
		if (isMuted(pcpe.getPlayer()) && !pcpe.getPlayer().hasPermission("iMine.unMuteAble")) {
			pcpe.setCancelled(true);
		}
	}

	public static boolean isMuted(OfflinePlayer pl) {
		return MUTED.contains(pl.getUniqueId());
	}

	@EventHandler
	public void updateVanish(VanishUpdateEvent vue) {
		if (!vanishAble) {
			return;
		}
		List<Player> nonHidden = new ArrayList<>();
		List<Player> hidden = new ArrayList<>();
		Bukkit.getOnlinePlayers().forEach(pl -> {
			iMinePlayer ipl = iMinePlayer.findPlayer(pl);
			if (ipl.isVanished()) {
				hidden.add(pl);
			} else {
				nonHidden.add(pl);
			}
		});
		Bukkit.getOnlinePlayers().forEach(pl -> {
			nonHidden.forEach(visible -> pl.showPlayer(visible));
			hidden.forEach(hide -> pl.hidePlayer(hide));
		});
	}

	@EventHandler
	public void updateVanish(PlayerJoinEvent pje) {
		updateVanish((VanishUpdateEvent) null);
	}

	@EventHandler
	public void joinDeop(PlayerJoinEvent pje) {
		if (pje.getPlayer().isOp()) {
			pje.getPlayer().setOp(false);
		}
	}

	@EventHandler
	public void chat(AsyncPlayerChatEvent apce) {
		Player pl = apce.getPlayer();
		String mssg = apce.getMessage();
		if (!pl.hasPermission("iMine.helpOverride")) {
			for (String shp : SHOP_FORMAT) {
				if (mssg.toLowerCase().contains(shp)) {
					pl.sendMessage(ColorUtil.replaceColors(
						"&6&lDo you like to purchase one of our fine VIP ranks or a whitelist? Click here &9&nshop.iMine.nl&6."));
					break;
				}
			}
			for (String bgs : BUGS_FORMAT) {
				if (mssg.toLowerCase().contains(bgs)) {
					pl.sendMessage(ColorUtil.replaceColors(
						"&6&lFound a bug or something else to report? Post it here &9&nbugs.iMine.nl&6."));
					break;
				}
			}
			for (String ts : TS_FORMAT) {
				if (mssg.toLowerCase().contains(ts)) {
					pl.sendMessage(
						ColorUtil.replaceColors("&6&lUse our official TeamSpeak3 server: &9&nts.iMine.nl&6."));
					break;
				}
			}
		}
		for (String ts : REPORT_FORMAT) {
			if (mssg.toLowerCase().contains(ts)) {
				pl.sendMessage(ColorUtil
						.replaceColors("&c&lInstead of calling someone a hacker, report it to our moderation team."));
				pl.sendMessage(ColorUtil.replaceColors(
					"&6To report: use '&c/report %s&6'. &6&o{Works even when admins are not online}", mssg));
				break;
			}
		}
	}

	@EventHandler
	public void onJoining(PlayerJoinEvent pje) {
		if (pje.getPlayer().hasPermission("iMine.autoGit")) {
			pje.getPlayer().performCommand("git -q");
		}
	}
}
