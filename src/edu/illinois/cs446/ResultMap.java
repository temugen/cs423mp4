package edu.illinois.cs446;

import java.util.concurrent.ConcurrentHashMap;

public class ResultMap extends ConcurrentHashMap<Integer, Integer> {
	private static final long serialVersionUID = 1L;

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
