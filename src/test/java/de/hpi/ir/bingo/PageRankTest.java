package de.hpi.ir.bingo;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class PageRankTest {

	@Test
	public void testCalculatePageRank() throws Exception {
		Int2ObjectMap<IntList> incomingLinks = new Int2ObjectOpenHashMap<>();
		incomingLinks.put(1, new IntArrayList(new int[]{2}));
		incomingLinks.put(2, new IntArrayList(new int[]{1,4}));
		//incomingLinks.put(4, new IntArrayList(new int[]{}));


		Int2DoubleMap ranks = PageRank.calculatePageRank(incomingLinks);

		assertThat(ranks.getOrDefault(1, Double.NaN)).isWithin(0.001).of(0.463);
		assertThat(ranks.getOrDefault(2, Double.NaN)).isWithin(0.001).of(0.486);
		assertThat(ranks.getOrDefault(4, Double.NaN)).isWithin(0.001).of(0.050);
	}

}