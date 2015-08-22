package nl.MakerTim.HubEssentials;

import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

public class BukkitListener implements Listener {

	public void onJoin(AsyncPlayerPreLoginEvent event) {
		if (!BukkitStarter.isDev(event.getUniqueId()) && BukkitStarter.plugin.devMode) {
			event.disallow(Result.KICK_FULL, "Server is now in dev mode.");
		}
	}
}
