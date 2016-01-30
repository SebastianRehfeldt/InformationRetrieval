package de.hpi.ir.bingo;

import java.util.Collections;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class PageRank {

	public static Int2DoubleMap calculatePageRank(Int2ObjectMap<IntList> incomingLinks) {
		int[] ids = getIds(incomingLinks);
		int n = ids.length;
		double d = 0.85;

		incomingLinks.defaultReturnValue(IntLists.EMPTY_LIST);

		Int2IntMap L = new Int2IntOpenHashMap(); // number of patents cited
		for (int to : ids) {
			for (int from : incomingLinks.get(to)) {
				L.put(from, L.get(from) + 1);
			}
		}

		Int2DoubleMap pr = new Int2DoubleOpenHashMap();
		for (int id : ids) {
			pr.put(id, 1.0 / n);
		}

		double diff = 1;
		int iter = 0;
		while (diff > 0.0000000001) {
			Int2DoubleMap prNew = new Int2DoubleOpenHashMap();
			for (int i : ids) {
				double sum = 0;
				for (int j : incomingLinks.get(i)) {
					sum += pr.get(j) / L.get(j);
				}
				prNew.put(i, (1 - d) / n + d * sum);
			}
			diff = 0;
			for (int id : ids) {
				diff += Math.abs(pr.get(id) - prNew.get(id));
			}
			System.out.printf("pagerank iteration: %d diff: %.10f\n", iter++, diff);
			pr = prNew;
		}

		return pr;
	}

	private static int[] getIds(Int2ObjectMap<IntList> incomingLinks) {
		IntSet ids = new IntOpenHashSet();
		ids.addAll(incomingLinks.keySet());
		for (IntList links : incomingLinks.values()) {
			ids.addAll(links);
		}
		return ids.toIntArray();
	}

}
