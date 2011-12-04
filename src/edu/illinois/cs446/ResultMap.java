package edu.illinois.cs446;

import java.util.concurrent.ConcurrentHashMap;

public class ResultMap extends ConcurrentHashMap<Integer, Integer> {
	private static final long serialVersionUID = 1L;

	/**
	 * Atomically increments a value in the hash map
	 * @param key
	 * @param times
	 */
	public void increment(Integer key, Integer times) {
		if (putIfAbsent(key, 1) == null) {
	        return;
	    }
		
		Integer count;
		do {
			count = get(key);
		} while(!replace(key, count, count + times));
	}
}
