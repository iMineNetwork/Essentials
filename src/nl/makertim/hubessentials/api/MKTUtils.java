package nl.makertim.hubessentials.api;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import nl.makertim.hubessentials.BukkitStarter;

public class MKTUtils {
	private static Map<Character, Integer> timeDic;
	private static final long MINUTES_IN_SECONDS = 60;
	private static final long HOURS_IN_SECONDS = MINUTES_IN_SECONDS * 60;
	private static final long DAYS_IN_SECONDS = HOURS_IN_SECONDS * 24;
	private static final long WEEKS_IN_SECONDS = DAYS_IN_SECONDS * 7;
	private static final long MONTHS_IN_SECONDS = DAYS_IN_SECONDS * 28;
	private static final long YEARS_IN_SECONDS = DAYS_IN_SECONDS * 356;

	private static int getFlag(char c) {
		if (timeDic == null) {
			timeDic = new HashMap<>();
			timeDic.put('s', Calendar.SECOND);
			timeDic.put('m', Calendar.MINUTE);
			timeDic.put('h', Calendar.HOUR);
			timeDic.put('u', Calendar.HOUR);
			timeDic.put('d', Calendar.DAY_OF_MONTH);
			timeDic.put('j', Calendar.YEAR);
			timeDic.put('y', Calendar.YEAR);
		}
		return timeDic.get(c);
	}

	public static boolean deleteDirectory(File directory) {
		if (directory.exists()) {
			File[] files = directory.listFiles();
			if (null != files) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						deleteDirectory(files[i]);
					} else {
						files[i].delete();
					}
				}
			}
		}
		return (directory.delete());
	}

	public static boolean isEqual(Player pl, Player pl2) {
		return pl.getUniqueId().equals(pl2.getUniqueId());
	}

	public static <T> boolean contains(Collection<T> list, T item) {
		boolean b = false;
		for (T listItem : list) {
			if (listItem.equals(item)) {
				b = true;
			}
		}
		return b;
	}

	public static <T> boolean contains(T[] list, T item) {
		boolean b = false;
		for (T listItem : list) {
			if (listItem.equals(item)) {
				b = true;
			}
		}
		return b;
	}

	public static Date fromShortString(String str) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(0));
		try {
			char timeChar = str.charAt(str.length() - 1);
			if (timeChar == 'w') {
				c.add(getFlag('d'), Integer.valueOf(str.substring(0, str.length() - 1)) * 7);
			} else {
				c.add(getFlag(timeChar), Integer.valueOf(str.substring(0, str.length() - 1)));
			}
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
		return c.getTime();
	}

	public static Date combine(Date first, Date toAdd) {
		return new Date(first.getTime() + toAdd.getTime());
	}

	public static String readableEnum(Enum<?> e) {
		return readableVar(e.toString());
	}

	public static String readableVar(String javaName) {
		javaName = javaName.toString().toLowerCase().replaceAll("_", " ");
		String ret = "";
		boolean CASE = true;
		for (char c : javaName.toString().toCharArray()) {
			if (Character.isSpaceChar(c)) {
				ret += c;
				continue;
			}
			if (!Character.isLetter(c)) {
				CASE = true;
				continue;
			}
			if (CASE) {
				ret += Character.toUpperCase(c);
				CASE = false;
			} else {
				ret += Character.toLowerCase(c);
			}
		}
		return ret;
	}

	public static String timeUntilNow(Date until) {
		String ret = "";
		long secondsBetween = (long) Math.ceil(((until.getTime() - new Date().getTime()) / 1000));
		if (secondsBetween < 0) {
			secondsBetween *= -1L;
		}
		double minus = 0;
		if (secondsBetween >= YEARS_IN_SECONDS) {
			minus = Math.floor(secondsBetween / YEARS_IN_SECONDS);
			secondsBetween -= (long) minus * YEARS_IN_SECONDS;
			ret += (int) minus + " years, ";
		}
		if (secondsBetween >= MONTHS_IN_SECONDS) {
			minus = Math.floor(secondsBetween / MONTHS_IN_SECONDS);
			secondsBetween -= (long) minus * MONTHS_IN_SECONDS;
			ret += (int) minus + " months, ";
		}
		if (secondsBetween >= WEEKS_IN_SECONDS) {
			minus = Math.floor(secondsBetween / WEEKS_IN_SECONDS);
			secondsBetween -= (long) minus * WEEKS_IN_SECONDS;
			ret += (int) minus + " weeks, ";
		}
		if (secondsBetween >= DAYS_IN_SECONDS) {
			minus = Math.floor(secondsBetween / DAYS_IN_SECONDS);
			secondsBetween -= (long) minus * DAYS_IN_SECONDS;
			ret += (int) minus + " days, ";
		}
		if (secondsBetween >= HOURS_IN_SECONDS) {
			minus = Math.floor(secondsBetween / HOURS_IN_SECONDS);
			secondsBetween -= (long) minus * HOURS_IN_SECONDS;
			ret += (int) minus + " hours, ";
		}
		if (secondsBetween >= MINUTES_IN_SECONDS) {
			minus = Math.floor(secondsBetween / MINUTES_IN_SECONDS);
			secondsBetween -= (long) minus * MINUTES_IN_SECONDS;
			ret += (int) minus + " minutes, ";
		}
		if (ret.endsWith(", ")) {
			ret = ret.substring(0, ret.length() - 2);
		} else if (ret.length() == 0) {
			ret = "Any minute";
		}
		return ret;
	}

	public static void sendPlayerToServer(final Player pl, final String serverID) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeUTF("Connect");
			dos.writeUTF(serverID);
			pl.sendPluginMessage(BukkitStarter.plugin, "BungeeCord", baos.toByteArray());
			baos.close();
			dos.close();
		} catch (Exception ex) {
			System.err.println("Player " + pl.getName() + " has not been sended to " + serverID + " because of "
					+ ex.getMessage());
			pl.sendMessage("You " + " have not been sended to " + serverID + " because of " + ex.getMessage());
		}
	}

	public static void kickPlayer(final Player pl, final String message) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeUTF("KickPlayer");
			dos.writeUTF(pl.getName());
			dos.writeUTF(message);
			pl.sendPluginMessage(BukkitStarter.plugin, "BungeeCord", baos.toByteArray());
			baos.close();
			dos.close();
		} catch (Exception ex) {
		}
	}
}
