package nl.makertim.hubessentials;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class Profiler implements Listener, Runnable {

	private static final Long DELAY = 20L * 60L * 5L;

	private final DatabaseManager db;
	private final String serverName;
	private double memUsed, memMax, memFree, memPercentageFree;

	public Profiler() {
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(BukkitStarter.plugin, new Lagg(), 100L, 1L);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(BukkitStarter.plugin, this, 1000L, DELAY);
		db = BukkitStarter.plugin.getDB();
		serverName = BukkitStarter.plugin.getConfig().getString("ServerName", "unknown");
	}

	public void updateMemoryStats() {
		// alles in MB's gemeten
		this.memUsed = ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576.0D);
		this.memMax = (Runtime.getRuntime().maxMemory() / 1048576.0D);
		this.memFree = (this.memMax - this.memUsed);
		this.memPercentageFree = (100.0D / this.memMax * this.memFree);
	}

	@Override
	public void run() {
		updateMemoryStats();
		db.insertQuery(String.format(
				"INSERT INTO ServerLog (TimeChecked, ServerName, PlayerCount, TPS, RamUsage) VALUES (NOW(), '%s', '%s', '%s', '%s');",
				DatabaseManager.prepaireString(serverName),
				DatabaseManager.prepaireString(Bukkit.getOnlinePlayers().size()),
				DatabaseManager.prepaireString(Math.round(Lagg.getTPS())),
				DatabaseManager.prepaireString(Math.round(memPercentageFree))));
	}

	private static class Lagg implements Runnable {
		public final static long[] TICKS = new long[600];
		public static int TICK_COUNT = 0;

		public static double getTPS() {
			return getTPS(100);
		}

		public static double getTPS(int ticks) {
			if (TICK_COUNT <= ticks) {
				return 20.0D;
			}
			int target = (TICK_COUNT - 1 - ticks) % TICKS.length;
			long elapsed = System.currentTimeMillis() - TICKS[target];
			return ticks / (elapsed / 1000.0D);
		}

		public void run() {
			TICKS[(TICK_COUNT % TICKS.length)] = System.currentTimeMillis();
			TICK_COUNT += 1;
		}
	}
}
