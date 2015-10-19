package de.hpi.ir.bingo;

import com.google.common.base.Objects;
import com.google.common.base.Verify;

public class PatentData {
	private final int patentId;
	private final String title;

	public PatentData(int patentId, String title) {
		this.patentId = patentId;
		this.title = title;
	}

	public int getPatentId() {
		return patentId;
	}

	public String getTitle() {
		return title;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
						.add("patentId", patentId)
						.add("title", title)
						.toString();
	}
}
