package de.hpi.ir.bingo.index;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.VerifyException;
import com.google.common.collect.Maps;

public class TableReaderWriterTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();


    @Test
    public void testReadNext() throws Exception {
        Path tmpFile = folder.newFile().toPath();

        TableWriter<String> writer = new TableWriter<>(tmpFile, 1, false);
        writer.put("k1", "v1");
        writer.put("k2", "v2");
        writer.close();

        TableReader<String> reader = new TableReader<>(tmpFile);
        assertThat(reader.readNext()).isEqualTo(Maps.immutableEntry("k1", "v1"));
        assertThat(reader.readNext()).isEqualTo(Maps.immutableEntry("k2", "v2"));
        assertThat(reader.readNext()).isNull();
        reader.close();
    }


    @Test(expected = VerifyException.class)
    public void testWriterChecksOrder() throws Exception {
        Path tmpFile = folder.newFile().toPath();
        try(TableWriter<String> writer = new TableWriter<>(tmpFile, 1, false)) {
            writer.put("k2", "v1");
            writer.put("k1", "v2");
        }
    }
}