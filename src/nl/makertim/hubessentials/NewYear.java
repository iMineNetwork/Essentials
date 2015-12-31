package nl.makertim.hubessentials;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class NewYear implements Listener, Runnable {

	private static final long DELAY = 20 * 5;
	private static final Random RAND = new Random();

	public NewYear() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(BukkitStarter.plugin, this, 0L, DELAY);
	}

	private static void generateFireworkAt(Location loc) {
		final Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
		FireworkMeta fem = firework.getFireworkMeta();
		for (int i = 0; i < RAND.nextInt(3) + 1; i++) {
			fem.addEffect(randomEffect());
		}
		firework.setFireworkMeta(fem);
		new BukkitRunnable() {
			@Override
			public void run() {
				firework.detonate();
			}
		}.runTaskLater(BukkitStarter.plugin, RAND.nextInt(60) + 5L);
	}

	private static FireworkEffect randomEffect() {
		int cs = RAND.nextInt(2) + 1;
		Color[] colors = new Color[cs];
		for (int i = 0; i < cs; i++) {
			colors[i] = Color.fromRGB(RAND.nextInt(255), RAND.nextInt(255), RAND.nextInt(255));
		}
		int fcs = RAND.nextInt(2) + 1;
		Color[] fcolors = new Color[fcs];
		for (int i = 0; i < fcs; i++) {
			fcolors[i] = Color.fromRGB(RAND.nextInt(255), RAND.nextInt(255), RAND.nextInt(255));
		}
		Type type = Type.values()[RAND.nextInt(Type.values().length)];
		boolean flicker = RAND.nextBoolean();
		boolean trail = RAND.nextBoolean();
		return FireworkEffect.builder().withColor(colors).with(type).withFade(fcolors).flicker(flicker).trail(trail)
				.build();
	}

	@Override
	public void run() {
		for (Player pl : Bukkit.getOnlinePlayers()) {
			if (!pl.hasPermission("iMine.VIP+")) {
				if (RAND.nextBoolean()) {
					return;
				}
			}
			if (!pl.hasPermission("iMine.VIP")) {
				if (RAND.nextBoolean()) {
					return;
				}
			}
			generateFireworkAt(pl.getLocation());
		}
	}
}
