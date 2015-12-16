package de.hpi.ir.bingo.index;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.hpi.ir.bingo.PatentData;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public final class TableUtil {

	private static final ThreadLocal<Kryo> kryos = new ThreadLocal<Kryo>() {
		protected Kryo initialValue() {
			Kryo kryo = new Kryo();
			kryo.setReferences(false);
			kryo.setRegistrationRequired(true);
			//kryo.register(PostingList.class, new PostingList.PostingListSerializer());
			kryo.register(PatentData.class, new PatentData.PatentDataSerializer());
			kryo.register(TableIndex.class, new TableIndex.TableIndexSerializer());
			kryo.register(HashMap.class);
			kryo.register(ArrayList.class);
			return kryo;
		}
	};

	static Path getIndexPath(Path file) {
		return Paths.get(file.toString() + ".index");
	}

	public static Output createOutput(Path file) {
		try {
			return new Output(Files.newOutputStream(file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static RandomAccessInput createInput(Path file) {
		try {
			return new RandomAccessInput(new RandomAccessFile(file.toFile(), "r"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Kryo getKryo() {
		return kryos.get();
	}

	public static <T> Serializer<T> getDefaultSerializerIfNull(Serializer<T> serializer, Class<T> clazz) {
		if (serializer == null) {
			return getKryo().getSerializer(clazz);
		} else {
			return serializer;
		}
	}

	public static TableIndex getTableIndex(Path tableFile) {
		Path indexPath = TableUtil.getIndexPath(tableFile);
		Input indexReader = TableUtil.createInput(indexPath);
		TableIndex index = TableUtil.getKryo().readObject(indexReader, TableIndex.class);
		indexReader.close();
		return index;
	}
}
