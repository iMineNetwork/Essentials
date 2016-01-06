package nl.makertim.essentials.api;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import nl.makertim.essentials.BukkitStarter;

public class PlayerGetter {

	private PlayerGetter() {
	}

	public static List<String> getNamesLike(String name) {
		List<String> ret = PlayerGetter.getAllNames(name);
		if (!ret.isEmpty()) {
			return ret;
		}
		name = DatabaseManager.prepareString(name);
		try {
			ResultSet rs = BukkitStarter.plugin.getDB()
					.doQuery("SELECT LastName FROM UUID_Table WHERE LastName LIKE '%" + name + "%';");
			while (rs.next()) {
				ret.add(rs.getString("LastName"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	}

	public static List<UUID> getUuidsLike(String name) {
		List<UUID> ret = new ArrayList<>();
		List<OfflinePlayer> offlinePlayers = PlayerGetter.getAll(name);
		if (!offlinePlayers.isEmpty()) {
			for (OfflinePlayer opl : offlinePlayers) {
				ret.add(opl.getUniqueId());
			}
			return ret;
		}
		name = DatabaseManager.prepareString(name);
		try {
			ResultSet rs = BukkitStarter.plugin.getDB()
					.doQuery("SELECT UUID FROM UUID_Table WHERE LastName LIKE '%" + name + "%';");
			while (rs.next()) {
				ret.add(UUID.fromString(rs.getString("UUID")));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	}

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
