package de.hpi.ir.bingo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.ArrayList;
import java.util.List;

public final class PostingListItem {
	private final int patentId;
	private IntArrayList positions;

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

	public PostingListItem(int id, int[] positions) {
		this(id, IntArrayList.wrap(positions));
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
	
	public PostingListItem union(PostingListItem item){
		int[] elements = positions.elements();
		int[] elements2 = item.positions.elements();
		IntArrayList result = new IntArrayList();
		int i1 = 0, i2 = 0;
		while (i1 < elements.length && i2 < elements2.length) {
			int v1 = elements[i1] + 1;
			int v2 = elements2[i2];
			if (v1 < v2){
				i1++;
			}
			else if (v1 == v2){
				result.add(v1);
				i1++;
				i2++;
			}
			else{
				i2++;
			}				
		}
		return new PostingListItem(patentId, result);
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
	
	@Override
	public String toString() {
	return MoreObjects.toStringHelper(this).add("id", patentId).add("positions", positions).toString();
	}
}
