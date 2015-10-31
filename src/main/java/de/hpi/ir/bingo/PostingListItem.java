package de.hpi.ir.bingo;

import com.google.common.base.Objects;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.ArrayList;
import java.util.List;

public final class PostingListItem {
	private final int patentId;
	private final IntArrayList positions;

	/*private PostingListItem() {
		this(-1, new IntArrayList());
	}*/

	public PostingListItem(int patentId, int position) {
		this(patentId,  new IntArrayList());
		this.positions.add(position);
	}

	public PostingListItem(int id, IntArrayList posList) {
		this.patentId = id;
		this.positions = posList;
	}

	public void addPosition(int position) {
		positions.add(position);
	}

	public int getPatentId() {
		return patentId;
	}

	public IntArrayList getPositions() {
		return positions;
	}

	public void print() {
		System.out.print("\t Patent: " + patentId + "\n \t \t Position: ");
		for (int position : positions) {
			System.out.print(position + ", ");
		}
		System.out.println("");
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PostingListItem that = (PostingListItem) o;
		return Objects.equal(patentId, that.patentId) &&
				Objects.equal(positions, that.positions);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(patentId, positions);
	}
}
