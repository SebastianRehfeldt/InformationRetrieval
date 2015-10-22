package de.hpi.ir.bingo.index;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

final class TableUtil {

    static Path getIndexPath(Path file) {
        return Paths.get(file.toString() + ".index");
    }

    static Output createOutput(Path file) {
        try {
            return new Output(Files.newOutputStream(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static RandomAccessInput createInput(Path file) {
        try {
            return new RandomAccessInput(new RandomAccessFile(file.toFile(), "r"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final ThreadLocal<Kryo> kryos = new ThreadLocal<Kryo>() {
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.setReferences(false);
            kryo.setRegistrationRequired(true);
            kryo.register(TableIndex.class, new TableIndex.TableIndexSerializer());
            return kryo;
        }
    };

    static Kryo getKryo() {
        return kryos.get();
    }

}
