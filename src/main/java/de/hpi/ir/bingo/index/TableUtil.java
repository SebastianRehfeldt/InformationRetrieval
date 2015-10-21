package de.hpi.ir.bingo.index;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.esotericsoftware.kryo.io.Output;

class TableUtil {

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

}
