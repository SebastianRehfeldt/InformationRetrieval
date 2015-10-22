package de.hpi.ir.bingo;

import java.io.Serializable;

import com.google.common.base.MoreObjects;

public class PatentData implements Serializable{
	private final int patentId;
	private final String title;
	private final String abstractText;

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

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
						.add("patentId", patentId)
						.add("title", title)
						.toString();
	}
}
