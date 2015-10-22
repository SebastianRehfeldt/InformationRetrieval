package de.hpi.ir.bingo;

import java.util.ArrayList;

public class PostingListItem {
	private int patentId;
	private ArrayList<Integer> positions = new ArrayList<Integer>();

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
	
	public ArrayList<Integer> getPositions(){
		return positions;
	}
	
	public void print(){
		System.out.print("\t Patent: "+patentId+"\n \t \t Position: ");
		for(int position : positions){
			System.out.print(position+", ");
		}
		System.out.println("");
	}
	
}
