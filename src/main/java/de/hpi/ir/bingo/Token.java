package de.hpi.ir.bingo;

import com.google.common.base.Objects;

public final class Token {
	public final String text;
	public final int begin;
	public final int end;

	public Token(String text, int begin, int end) {
		this.text = text;
		this.begin = begin;
		this.end = end;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("text", text)
				.add("begin", begin)
				.add("end", end)
				.toString();
	}
}
