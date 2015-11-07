package de.hpi.ir.bingo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Verify;
import com.google.common.collect.Lists;

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
		this(Lists.newArrayList());
	}

	public PostingList(List<PostingListItem> items) {
		this.items = items;
	}

	public Collection<PostingListItem> getItems() {
		return items;
	}

	public void addItem(PostingListItem item) {
		Verify.verify(items.isEmpty() || items.get(items.size() - 1).getPatentId() < item.getPatentId());
		items.add(item);
	}

	public PostingList and(PostingList other) {
		List<PostingListItem> items2 = other.items;
		int i1 = 0, i2 = 0;
		PostingList result = new PostingList();
		while (i1 < items.size() && i2 < items2.size()) {
			if (items.get(i1).getPatentId() < items2.get(i2).getPatentId()) {
				i1++;
			} else if (items.get(i1).getPatentId() == items2.get(i2).getPatentId()) {
				PostingListItem and = items.get(i1).merge(items2.get(i2));
				result.addItem(and);
				i1++;
				i2++;
			} else {
				i2++;
			}
		}
		return result;
	}

	public PostingList or(PostingList other) {
		List<PostingListItem> items2 = other.items;
		int i1 = 0, i2 = 0;
		PostingList result = new PostingList();
		while (i1 < items.size() && i2 < items2.size()) {
			if (items.get(i1).getPatentId() < items2.get(i2).getPatentId()) {
				result.addItem(items.get(i1));
				i1++;
			} else if (items.get(i1).getPatentId() == items2.get(i2).getPatentId()) {
				PostingListItem and = items.get(i1).merge(items2.get(i2));
				result.addItem(and);
				i1++;
				i2++;
			} else {
				result.addItem(items2.get(i2));
				i2++;
			}
		}
		while (i1 < items.size()) {
			result.addItem(items.get(i1++));
		}
		while (i2 < items2.size()) {
			result.addItem(items2.get(i2++));
		}
		return result;
	}

	public PostingList not(PostingList other) {
		List<PostingListItem> items2 = other.items;
		int i1 = 0, i2 = 0;
		PostingList result = new PostingList();
		while (i1 < items.size() && i2 < items2.size()) {
			if (items.get(i1).getPatentId() < items2.get(i2).getPatentId()) {
				result.addItem(items.get(i1));
				i1++;
			} else if (items.get(i1).getPatentId() == items2.get(i2).getPatentId()) {
				i1++;
				i2++;
			} else {
				i2++;
			}
		}
		while (i1 < items.size()) {
			result.addItem(items.get(i1++));
		}
		return result;
	}

	public PostingList union(PostingList postingList) {
		List<PostingListItem> items2 = postingList.items;
		int i1 = 0, i2 = 0;
		PostingList result = new PostingList();
		while (i1 < items.size() && i2 < items2.size()) {
			if (items.get(i1).getPatentId() < items2.get(i2).getPatentId()) {
				i1++;
			} else if (items.get(i1).getPatentId() == items2.get(i2).getPatentId()) {
				PostingListItem union = items.get(i1).union(items2.get(i2));
				if (union.getPositions().size() > 0) {
					result.addItem(union);
				}
				i1++;
				i2++;
			} else {
				i2++;
			}
		}
		return result;
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
			List<PostingListItem> items = new ArrayList<>(size);

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

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("items", items).toString();
	}
}
