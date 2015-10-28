package de.hpi.ir.bingo.index;

import com.esotericsoftware.kryo.io.Input;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;

final class RandomAccessInput extends Input {

	private final RandomAccessFile randomAccessFile;

	public RandomAccessInput(RandomAccessFile randomAccessFile) {
		super(Channels.newInputStream(randomAccessFile.getChannel()));
		this.randomAccessFile = randomAccessFile;
	}

	public void setStreamPosition(long position) {
		try {
			randomAccessFile.seek(position);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.setLimit(0);
		this.rewind();
	}
}
