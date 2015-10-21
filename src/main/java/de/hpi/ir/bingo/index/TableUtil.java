package de.hpi.ir.bingo.index;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

class TableUtil {

    static Path getIndexPath(Path file) {
        return Paths.get(file.toString() + ".index");
    }

    static ObjectOutputStream objectOutputStream(FileOutputStream fos) {
        try {
            return new ObjectOutputStream(fos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static FileOutputStream createOutputStream(Path file) {
        try {
            return new FileOutputStream(file.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static ObjectInputStream objectInputStream(FileInputStream fis) {
        try {
            return new ObjectInputStream(fis);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static FileInputStream createInputStream(Path file) {
        try {
            return new FileInputStream(file.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
