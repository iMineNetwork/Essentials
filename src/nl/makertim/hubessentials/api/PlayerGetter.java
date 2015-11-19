package nl.makertim.hubessentials.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerGetter {

	@CheckForNull
	public static OfflinePlayer get(String name) {
		OfflinePlayer ret = getOnline(name);
		if (ret == null) {
			ret = getOflline(name);
		}
		return ret;
	}

	public static List<String> getAllNames(String name) {
		List<String> ret = new ArrayList<>();
		ret.addAll(getAllOnlineNames(name));
		for (String opl : getAllOfflineNames(name)) {
			if (!ret.contains(opl)) {
				ret.add(opl);
			}
		}
		return ret;
	}

	public static List<OfflinePlayer> getAll(String name) {
		List<OfflinePlayer> ret = new ArrayList<>();
		ret.addAll(getAllOnline(name));
		for (OfflinePlayer opl : getAllOffline(name)) {
			if (!ret.contains(opl)) {
				ret.add(opl);
			}
		}
		return ret;
	}

	@CheckForNull
	public static OfflinePlayer get(String name, Player pls) {
		OfflinePlayer ret = getOnline(name, pls);
		if (ret == null) {
			ret = getOflline(name);
		}
		return ret;
	}

	public static List<String> getAllNames(String name, Player pls) {
		List<String> ret = new ArrayList<>();
		ret.addAll(getAllOnlineNames(name, pls));
		for (String opl : getAllOfflineNames(name)) {
			if (!ret.contains(opl)) {
				ret.add(opl);
			}
		}
		return ret;
	}

	public static List<OfflinePlayer> getAll(String name, Player pls) {
		List<OfflinePlayer> ret = new ArrayList<>();
		ret.addAll(getAllOnline(name, pls));
		for (OfflinePlayer opl : getAllOffline(name)) {
			if (!ret.contains(opl)) {
				ret.add(opl);
			}
		}
		return ret;
	}

	@CheckForNull
	public static Player getOnline(String name) {
		List<Player> ret = getAllOnline(name);
		if (ret.isEmpty()) {
			return null;
		}
		return ret.get(0);
	}

	public static List<String> getAllOnlineNames(String name) {
		List<String> ret = new ArrayList<>();
		for (Player pl : getAllOnline(name)) {
			if (pl.getName().toLowerCase().contains(name.toLowerCase())) {
				ret.add(pl.getName());
			}
		}
		return ret;
	}

	public static List<Player> getAllOnline(String name) {
		List<Player> ret = new ArrayList<>();
		for (Player pl : Bukkit.getOnlinePlayers()) {
			if (pl.getName().toLowerCase().contains(name.toLowerCase())) {
				ret.add(pl);
			}
		}
		Collections.sort(ret, new PlayerSearchSorter(name));
		return ret;
	}

	@CheckForNull
	public static Player getOnline(String name, Player pls) {
		List<Player> ret = getAllOnline(name, pls);
		if (ret.isEmpty()) {
			return null;
		}
		return ret.get(0);
	}

	public static List<String> getAllOnlineNames(String name, Player pls) {
		List<String> ret = new ArrayList<>();
		for (Player pl : getAllOnline(name)) {
			if (pl.getName().toLowerCase().contains(name.toLowerCase())) {
				if (pls.canSee(pl)) {
					ret.add(pl.getName());
				}
			}
		}
		return ret;
	}

	public static List<Player> getAllOnline(String name, Player pls) {
		List<Player> ret = new ArrayList<>();
		for (Player pl : Bukkit.getOnlinePlayers()) {
			if (pl.getName().toLowerCase().contains(name.toLowerCase())) {
				if (pls.canSee(pl)) {
					ret.add(pl);
				}
			}
		}
		Collections.sort(ret, new PlayerSearchSorter(name));
		return ret;
	}

	@CheckForNull
	public static OfflinePlayer getOflline(String name) {
		List<OfflinePlayer> ret = getAllOffline(name);
		if (ret.isEmpty()) {
			return null;
		}
		return ret.get(0);
	}

	public static List<String> getAllOfflineNames(String name) {
		List<String> ret = new ArrayList<>();
		for (OfflinePlayer pl : getAllOffline(name)) {
			if (pl.getName().toLowerCase().contains(name.toLowerCase())) {
				ret.add(pl.getName());
			}
		}
		return ret;
	}

	public static List<OfflinePlayer> getAllOffline(String name) {
		List<OfflinePlayer> ret = new ArrayList<>();
		for (OfflinePlayer pl : Bukkit.getOfflinePlayers()) {
			if (pl.getName().toLowerCase().contains(name.toLowerCase())) {
				ret.add(pl);
			}
		}
		Collections.sort(ret, new PlayerSearchSorter(name));
		return ret;
	}
}