package nl.makertim.hubessentials;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MapCountSorter<E, U> implements Comparator<E> {

	public static enum Sort {
		ASC, DESC
	}

	private final Map<E, List<U>> map;
	private final List<E> order;
	private final Sort sort;

	private MapCountSorter(Map<E, List<U>> map, Sort direction) {
		this.map = map;
		this.sort = direction;
		this.order = new ArrayList<E>(map.keySet());
		Collections.sort(order, this);
	}

	public static <T, U> List<T> getOrder(Map<T, List<U>> map, Sort sort) {
		return new MapCountSorter<T, U>(map, sort).order;
	}

	@Override
	public int compare(E obj0, E obj1) {
		if (map.containsKey(obj0) && map.containsKey(obj1)) {
			List<? extends Object> list0 = map.get(obj0);
			List<? extends Object> list1 = map.get(obj1);
			if (list0 != null && list1 != null) {
				if (sort == Sort.ASC) {
					return list0.size() - list1.size();
				} else {
					return list1.size() - list0.size();
				}
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}
}
