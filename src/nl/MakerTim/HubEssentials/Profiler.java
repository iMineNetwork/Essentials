package nl.MakerTim.HubEssentials;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class Profiler implements Listener, Runnable {

	private static final Long DELAY = 20L * 60L;

	private final DatabaseManager db;
	private final String serverName;
	private double memUsed, memMax, memFree, memPercentageFree;

	public Profiler() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(BukkitStarter.plugin, this, DELAY, DELAY);
		db = BukkitStarter.plugin.getDB();
		serverName = BukkitStarter.plugin.getConfig().getString("ServerName", "unknown");
	}

	public void updateMemoryStats() {
		this.memUsed = ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576.0D);
		this.memMax = (Runtime.getRuntime().maxMemory() / 1048576.0D);
		this.memFree = (this.memMax - this.memUsed);
		this.memPercentageFree = (100.0D / this.memMax * this.memFree);
	}

	@Override
	public void run() {
		
	}
}
