package nl.makertim.essentials;

import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import nl.imine.api.db.DatabaseManager;

public class Profiler implements Listener, Runnable {

	private static final Long DELAY = 20L * 60L * 5L;

	private final DatabaseManager db;
	private final String serverName;
	private double memUsed, memMax, memFree, memPercentageFree;

	public Profiler() {
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(BukkitStarter.plugin, new Lagg(), 100L, 1L);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(BukkitStarter.plugin, this, 200L, DELAY);
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
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		minute = minute - (minute % 5);
		db.insertQuery(String
				.format("INSERT INTO ServerLog (TimeChecked, ServerName, PlayerCount, TPS, RamUsage) VALUES ('%s', '%s', '%s', '%s', '%s');",
						DatabaseManager
								.prepareString(String.format("%d-%d-%d %d:%d:00", year, month, day, hour, minute)),
						DatabaseManager.prepareString(serverName),
						DatabaseManager.prepareString(Bukkit.getOnlinePlayers().size()),
						DatabaseManager.prepareString(Math.round(Lagg.getTPS())),
						DatabaseManager.prepareString(Math.round(memPercentageFree))));
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
