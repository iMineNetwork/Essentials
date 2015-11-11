package nl.makertim.hubessentials.api;

import java.util.Comparator;

import org.bukkit.OfflinePlayer;

public class PlayerSearchSorter implements Comparator<OfflinePlayer> {

	private final String toFind;

	public PlayerSearchSorter(String searchingFor) {
		toFind = searchingFor;
	}

	@Override
	public int compare(OfflinePlayer p1, OfflinePlayer p2) {
		if (toFind.isEmpty()) {
			return p1.getName().toLowerCase().compareTo(p2.getName().toLowerCase());
		}
		return p1.getName().toLowerCase().indexOf(toFind.toLowerCase())
				- p2.getName().toLowerCase().indexOf(toFind.toLowerCase());
	}
}
