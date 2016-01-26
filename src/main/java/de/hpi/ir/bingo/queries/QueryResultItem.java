package de.hpi.ir.bingo.queries;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Verify;

import de.hpi.ir.bingo.PostingListItem;

public class QueryResultItem {
	private final PostingListItem item;
	private double score;
	private String snippet;

	public QueryResultItem(PostingListItem item, double score, String snippet) {
		this.item = item;
		this.score = score;
		this.snippet = snippet;
	}

	public int getPatentId() {
		return item.getPatentId();
	}

	public PostingListItem getItem() {
		return item;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		Verify.verify(this.score == 0, "only allowed once!");
		this.score = score;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.addValue(item)
				.add("score", score)
				.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof QueryResultItem)) return false;
		QueryResultItem that = (QueryResultItem) o;
		return Double.compare(that.score, score) == 0 &&
				Objects.equal(item, that.item);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(item, score);
	}

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		Verify.verify(this.snippet == null, "snippet should only be set once");
		this.snippet = snippet;
	}

	public boolean hasTermInTitle() {
		if (item.getPositions().isEmpty()){
			return false;
		}
		return item.getPositions().get(0) < item.getTitleWordCount();
	}
	public boolean hasTermInAbstract() {
		if (item.getPositions().isEmpty()){
			return false;
		}
		return item.getPositions().get(0) < item.getTitleWordCount() + item.getAbstractWordCount();
	}

	public QueryResultItem withScore(double score) {
		return new QueryResultItem(item, this.score, snippet);
	}
}
