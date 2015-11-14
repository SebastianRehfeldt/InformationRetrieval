package de.hpi.ir.bingo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import it.unimi.dsi.fastutil.ints.IntArrayList;

public final class PostingListItem {
	private final int patentId;
	private final IntArrayList positions;
	private final int documentWordCount;

	/*private PostingListItem() {
		this(-1, new IntArrayList());
	}*/

	public PostingListItem(int patentId, int position, int documentWordCount) {
		this(patentId, new IntArrayList(), documentWordCount);
		this.positions.add(position);
	}

	public PostingListItem(int id, IntArrayList posList, int documentWordCount) {
		this.patentId = id;
		this.positions = posList;
		this.documentWordCount = documentWordCount;
	}

	public PostingListItem(int id, int[] positions, int documentWordCount) {
		this(id, IntArrayList.wrap(positions), documentWordCount);
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

	/**
	 * Combines this list with the list of a word that should be next in a phrase query.
	 * The result contains the positions of all terms from {@code item} that have a matching position in {@code this}
	 */
	public PostingListItem combinePhrase(PostingListItem item) {
		int[] elements = positions.elements();
		int[] elements2 = item.positions.elements();
		IntArrayList result = new IntArrayList();
		int i1 = 0, i2 = 0;
		while (i1 < elements.length && i2 < elements2.length) {
			int v1 = elements[i1] + 1;
			int v2 = elements2[i2];
			if (v1 < v2) {
				i1++;
			} else if (v1 == v2) {
				result.add(v1);
				i1++;
				i2++;
			} else {
				i2++;
			}
		}
		return new PostingListItem(patentId, result, documentWordCount);
	}

	public PostingListItem merge(PostingListItem item) {
		int[] elements = positions.elements();
		int[] elements2 = item.positions.elements();
		IntArrayList result = new IntArrayList();
		int i1 = 0, i2 = 0;
		while (i1 < elements.length && i2 < elements2.length) {
			int v1 = elements[i1];
			int v2 = elements2[i2];
			if (v1 < v2) {
				result.add(v1);
				i1++;
			} else if (v1 == v2) {
				result.add(v1);
				i1++;
				i2++;
			} else {
				result.add(v2);
				i2++;
			}
		}
		while (i1 < elements.length) {
			result.add(elements[i1++]);
		}
		while (i2 < elements2.length) {
			result.add(elements2[i2++]);
		}
		return new PostingListItem(patentId, result,documentWordCount);
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

	public double getTermFrequency() {
		return positions.size()/(double)documentWordCount;
	}
	
	public int getDocumentWordCount() {
		return documentWordCount;
	}
}
