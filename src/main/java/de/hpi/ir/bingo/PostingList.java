package de.hpi.ir.bingo;

import com.google.common.base.Objects;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class PostingList {
	public static final Serializer<PostingList> NORMAL_SERIALIZER = new PostingListSerializer();
	public static final Serializer<PostingList> COMPRESSING_SERIALIZER = new PostingListCompressingSerializer();
	private final List<PostingListItem> items;

	public PostingList() {
		this(new ArrayList<>());
	}

	public PostingList(List<PostingListItem> items) {
		this.items = items;
	}

	public Collection<PostingListItem> getItems() {
		return items;
	}

	public void addItem(PostingListItem item) {
		items.add(item);
	}

	public void addAll(PostingList list) {
		items.addAll(list.items);
	}

	private static class PostingListCompressingSerializer extends Serializer<PostingList> {
		public void write(Kryo kryo, Output output, PostingList list) {
			output.writeVarInt(list.items.size(), true);
			int lastId = 0;
			for (PostingListItem item : list.items) {
				int id = item.getPatentId();
				output.writeVarInt(id - lastId, true);
				lastId = id;

				IntArrayList positions = item.getPositions();
				output.writeVarInt(positions.size(), true);
				int lastPos = 0;
				for (int pos : positions) {
					output.writeVarInt(pos - lastPos, true);
					lastPos = pos;
				}
			}
		}

		public PostingList read(Kryo kryo, Input input, Class<PostingList> type) {
			int size = input.readVarInt(true);
			ArrayList<PostingListItem> items = new ArrayList<>(size);
			int lastId = 0;
			for (int i = 0; i < size; i++) {
				int id = input.readVarInt(true) + lastId;
				lastId = id;
				int posSize = input.readVarInt(true);
				IntArrayList posList = new IntArrayList(posSize);
				int lastPos = 0;
				for (int i1 = 0; i1 < posSize; i1++) {
					int pos = input.readVarInt(true) + lastPos;
					lastPos = pos;
					posList.add(pos);
				}
				items.add(new PostingListItem(id, posList));
			}
			return new PostingList(items);
		}
	}

	private static class PostingListSerializer extends Serializer<PostingList> {
		public void write(Kryo kryo, Output output, PostingList list) {
			output.writeInt(list.items.size());
			for (PostingListItem item : list.items) {
				output.writeInt(item.getPatentId());
				IntArrayList positions = item.getPositions();
				output.writeInt(positions.size());
				for (int pos : positions) {
					output.writeInt(pos);
				}
			}
		}

		public PostingList read(Kryo kryo, Input input, Class<PostingList> type) {
			int size = input.readInt();
			ArrayList<PostingListItem> items = new ArrayList<>(size);
			for (int i = 0; i < size; i++) {
				int id = input.readInt();
				int posSize = input.readInt();
				IntArrayList posList = new IntArrayList(posSize);
				for (int i1 = 0; i1 < posSize; i1++) {
					int pos = input.readInt();
					posList.add(pos);
				}
				items.add(new PostingListItem(id, posList));
			}
			return new PostingList(items);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PostingList that = (PostingList) o;
		return Objects.equal(items, that.items);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(items);
	}
}
