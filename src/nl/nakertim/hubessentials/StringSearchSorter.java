package nl.nakertim.hubessentials;

import java.util.Comparator;

public class StringSearchSorter implements Comparator<String> {

	private final String toFind;

	public StringSearchSorter(String searchingFor) {
		toFind = searchingFor;
	}

	@Override
	public int compare(String s1, String s2) {
		if (toFind.isEmpty()) {
			return s1.toLowerCase().compareTo(s2.toLowerCase());
		}
		return s1.toLowerCase().indexOf(toFind.toLowerCase()) - s2.toLowerCase().indexOf(toFind.toLowerCase());
	}
}
