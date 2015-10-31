package de.hpi.ir.bingo.index;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Stopwatch;


public final class TableTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testIndexCreation() throws IOException {
        Path tmpFile = folder.newFile().toPath();

        TableWriter<String> writer = new TableWriter<>(tmpFile, true, 1, String.class, null);
        writer.put("k1", "v1");
        writer.put("k2", "v2");
        writer.close();

        Table<String> table = Table.open(tmpFile, String.class, null);
        assertThat(table.get("k2")).isEqualTo("v2");
        assertThat(table.get("k1")).isEqualTo("v1");
        assertThat(table.get("k11")).isNull();
        assertThat(table.get("k22")).isNull();
        table.close();
    }

    @Test
    public void testIndexCreationWithBuckets() throws IOException {
        Path tmpFile = folder.newFile().toPath();

        TableWriter<String> writer = new TableWriter<>(tmpFile, true, 4096, String.class, null);
        writer.put("k1", "v1");
        writer.put("k2", "v2");
        writer.put("k3", "v3");
        writer.put("k4", "v4");
        writer.put("k5", "v5");
        writer.close();

        Table<String> table = Table.open(tmpFile, String.class, null);
        assertThat(table.get("k1")).isEqualTo("v1");
        assertThat(table.get("k2")).isEqualTo("v2");
        assertThat(table.get("k22")).isNull();
        assertThat(table.get("k3")).isEqualTo("v3");
        assertThat(table.get("k4")).isEqualTo("v4");
        assertThat(table.get("k5")).isEqualTo("v5");
        assertThat(table.get("k6")).isNull();
        table.close();
    }

    @Test
    public void testIndexSpeed() throws IOException {
        Path tmpFile = folder.newFile().toPath();

        int insertCount = 100_000;
        int getCount = 100_000;
        int blockSize = 4096;

        String value = "QWERTZUPASDFGHJKLYXCVBNMQWERTZUIOPASDFGHJKLASDASDASDASDASDASDASD";
        Stopwatch sw = Stopwatch.createStarted();
        TableWriter<String> writer = new TableWriter<>(tmpFile, true, blockSize, String.class, null);
        for (int i = 0; i < insertCount; i++) {
            writer.put(String.format("key-%10d", i), value + i);
        }
        writer.close();
        System.out.println("writing table and index: " + sw);
        System.out.println("Table size: " + tmpFile.toFile().length() / 1000_000 + "MB");
        System.out.println("Index size: " + TableUtil.getIndexPath(tmpFile).toFile().length() / 1000 + "KB");

        sw.reset().start();
        Table<String> table = Table.open(tmpFile, String.class, null);
        System.out.println("reading index: " + sw);
        sw.reset().start();
        Random random = new Random(42);
        for (int i = 0; i < getCount; i++) {
            boolean missing = random.nextBoolean();
            String key = String.format("key-%10d", random.nextInt(insertCount)) + (missing ? "XX" : "");
            String result = table.get(key);
            if(!missing){
                assertThat(result).named(key).isNotNull();
            } else {
                assertThat(result).isNull();
            }

        }
        System.out.println("queries: " + sw);
        table.close();
    }
}