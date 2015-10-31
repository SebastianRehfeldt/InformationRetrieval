package de.hpi.ir.bingo;

import com.google.common.base.MoreObjects;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;

public class PatentData implements Serializable {
	private final int patentId;
	private final String title;
	private final String abstractText;

	private PatentData() {
		this(-1, "", "");
	}

	public PatentData(int patentId, String title, String abstractText) {
		this.patentId = patentId;
		this.title = title;
		this.abstractText = abstractText;
	}

	public int getPatentId() {
		return patentId;
	}

	public String getTitle() {
		return title;
	}

	public String getAbstractText() {
		return abstractText;
	}

	public static class PatentDataSerializer extends Serializer<PatentData> {
		public void write(Kryo kryo, Output output, PatentData data) {
			output.writeInt(data.patentId);
			output.writeString(data.title);
			output.writeString(data.abstractText);

		}

		public PatentData read(Kryo kryo, Input input, Class<PatentData> type) {
			int patentId = input.readInt();
			String title = input.readString();
			String abstractText = input.readString();
			return new PatentData(patentId, title, abstractText);
		}
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("patentId", patentId)
				.add("title", title)
				.toString();
	}
}
