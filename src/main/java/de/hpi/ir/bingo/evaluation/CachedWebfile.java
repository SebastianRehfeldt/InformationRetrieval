package de.hpi.ir.bingo.evaluation;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.hpi.ir.bingo.index.TableUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CachedWebfile {

	private Map<String, ArrayList<String>> cache;
	private Path cacheFile = Paths.get("webfilecache.bin");

	public List <String> getGoogleRanking(String query) {
		ArrayList<String> result = getCached(query);
		if (result == null) {
			result = new WebFile().getGoogleRanking(query);
			putRankingInCache(query, result);
		}
		return result;
	}

	private Map<String, ArrayList<String>> getCache() {
		if (cache == null) {
			if (cacheFile.toFile().exists()) {
				try {
					Input input = new Input(Files.newInputStream(cacheFile));
					cache = (Map<String, ArrayList<String>>) TableUtil.getKryo().readObject(input, HashMap.class);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				cache = new HashMap<>();
			}
		}
		return cache;
	}

	private void putRankingInCache(String query, ArrayList<String> ranking) {
		getCache().put(query, ranking);
		try {
			Output output = new Output(Files.newOutputStream(cacheFile));
			TableUtil.getKryo().writeObject(output, cache);
			output.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private ArrayList<String> getCached(String query) {
		return getCache().get(query);
	}

}
