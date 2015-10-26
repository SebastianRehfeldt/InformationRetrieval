package de.hpi.ir.bingo;

import com.google.common.base.Objects;

import java.util.ArrayList;
import java.util.List;

public final class PostingListItem {
	private final int patentId;
	private final List<Integer> positions = new ArrayList<>();

	private PostingListItem() {
		this(-1, -1);
	}

	public PostingListItem(int patentId, int position){
		this.patentId = patentId;
		this.positions.add(position);
	}
	
	public void addPosition(int position){
		positions.add(position);
	}
	
	public int getPatentId(){
		return patentId;
	}	
	
	public List<Integer> getPositions(){
		return positions;
	}
	
	public void print(){
		System.out.print("\t Patent: "+patentId+"\n \t \t Position: ");
		for(int position : positions){
			System.out.print(position+", ");
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
