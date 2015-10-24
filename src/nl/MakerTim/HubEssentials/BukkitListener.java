package nl.MakerTim.HubEssentials;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;

public class BukkitListener implements Listener {

	@EventHandler
	public void onJoin(AsyncPlayerPreLoginEvent event) {
		if (!BukkitStarter.isDev(event.getUniqueId()) && BukkitStarter.plugin.devMode) {
			event.disallow(Result.KICK_FULL, "Server is now in dev mode.");
		}
	}

	@EventHandler
	public void onJoining(PlayerJoinEvent pje) {
		if (pje.getPlayer().isOp() || BukkitStarter.isDev(pje.getPlayer().getUniqueId())) {
			pje.getPlayer().performCommand("git");
		}
	}
}
