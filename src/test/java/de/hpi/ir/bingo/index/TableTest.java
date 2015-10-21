package de.hpi.ir.bingo.index;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.ImmutableMap;

public class TableTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testIndexCreation() throws IOException {
        Path tmpFile = folder.newFile().toPath();

        TableWriter<String> writer = new TableWriter<>(tmpFile, 1, true);
        writer.put("k1", "v1");
        writer.put("k2", "v2");
        writer.close();

        Table<String> table = Table.<String>open(tmpFile);
        assertThat(table.get("k2")).isEqualTo("v2");
        assertThat(table.get("k1")).isEqualTo("v1");
        assertThat(table.get("k11")).isNull();
        assertThat(table.get("k22")).isNull();
        table.close();
    }

    @Test
    public void testIndexCreationWithBuckets() throws IOException {
        Path tmpFile = folder.newFile().toPath();

        TableWriter<String> writer = new TableWriter<>(tmpFile, 3, true);
        writer.put("k1", "v1");
        writer.put("k2", "v2");
        writer.put("k3", "v3");
        writer.put("k4", "v4");
        writer.put("k5", "v5");
        writer.close();

        Table<String> table = Table.<String>open(tmpFile);
        assertThat(table.get("k1")).isEqualTo("v1");
        assertThat(table.get("k2")).isEqualTo("v2");
        assertThat(table.get("k22")).isNull();
        assertThat(table.get("k3")).isEqualTo("v3");
        assertThat(table.get("k4")).isEqualTo("v4");
        assertThat(table.get("k5")).isEqualTo("v5");
        assertThat(table.get("k6")).isNull();
        table.close();
    }
}