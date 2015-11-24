package de.hpi.ir.bingo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.ints.IntArrayList;

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

	public List<PostingListItem> getItems() {
		return Collections.unmodifiableList(items);
	}

	public void addItem(PostingListItem item) {
		Verify.verify(items.isEmpty() || items.get(items.size() - 1).getPatentId() < item.getPatentId());
		items.add(item);
	}

	public PostingList and(PostingList other) {
		Preconditions.checkNotNull(other);
		List<PostingListItem> items2 = other.items;
		int i1 = 0, i2 = 0;
		PostingList result = new PostingList();
		while (i1 < items.size() && i2 < items2.size()) {
			PostingListItem p1 = items.get(i1);
			PostingListItem p2 = items2.get(i2);
			if (p1.getPatentId() < p2.getPatentId()) {
				i1++;
			} else if (p1.getPatentId() == p2.getPatentId()) {
				PostingListItem and = p1.merge(p2);
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
		Preconditions.checkNotNull(other);
		List<PostingListItem> items2 = other.items;
		int i1 = 0, i2 = 0;
		PostingList result = new PostingList();
		while (i1 < items.size() && i2 < items2.size()) {
			PostingListItem p1 = items.get(i1);
			PostingListItem p2 = items2.get(i2);
			if (p1.getPatentId() < p2.getPatentId()) {
				result.addItem(p1);
				i1++;
			} else if (p1.getPatentId() == p2.getPatentId()) {
				PostingListItem and = p1.merge(p2);
				result.addItem(and);
				i1++;
				i2++;
			} else {
				result.addItem(p2);
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
		Preconditions.checkNotNull(other);
		List<PostingListItem> items2 = other.items;
		int i1 = 0, i2 = 0;
		PostingList result = new PostingList();
		while (i1 < items.size() && i2 < items2.size()) {
			PostingListItem p1 = items.get(i1);
			PostingListItem p2 = items2.get(i2);
			if (p1.getPatentId() < p2.getPatentId()) {
				result.addItem(p1);
				i1++;
			} else if (p1.getPatentId() == p2.getPatentId()) {
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

	public PostingList combinePhrase(PostingList postingList) {
		Preconditions.checkNotNull(postingList);
		List<PostingListItem> items2 = postingList.items;
		int i1 = 0, i2 = 0;
		PostingList result = new PostingList();
		while (i1 < items.size() && i2 < items2.size()) {
			PostingListItem p1 = items.get(i1);
			PostingListItem p2 = items2.get(i2);
			if (p1.getPatentId() < p2.getPatentId()) {
				i1++;
			} else if (p1.getPatentId() == p2.getPatentId()) {
				PostingListItem union = p1.combinePhrase(p2);
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

	public int getDocumentCount() {
		return items.size();
	}

	private static class PostingListCompressingSerializer extends Serializer<PostingList> {
		public void write(Kryo kryo, Output output, PostingList list) {
			output.writeVarInt(list.items.size(), true);
			int lastId = 0;
			for (PostingListItem item : list.items) {
				int id = item.getPatentId();
				output.writeVarInt(id - lastId, true);
				output.writeVarInt(item.getDocumentWordCount(), true);
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
				int documentWordCount = input.readVarInt(true);
				int posSize = input.readVarInt(true);
				IntArrayList posList = new IntArrayList(posSize);
				int lastPos = 0;
				for (int i1 = 0; i1 < posSize; i1++) {
					int pos = input.readVarInt(true) + lastPos;
					lastPos = pos;
					posList.add(pos);
				}
				items.add(new PostingListItem(id, posList,documentWordCount));
			}
			return new PostingList(items);
		}
	}

	private static class PostingListSerializer extends Serializer<PostingList> {
		public void write(Kryo kryo, Output output, PostingList list) {
			output.writeInt(list.items.size());
			for (PostingListItem item : list.items) {
				output.writeInt(item.getPatentId());
				output.writeInt(item.getDocumentWordCount());
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
				int documentWordCount = input.readInt();
				int posSize = input.readInt();
				IntArrayList posList = new IntArrayList(posSize);
				for (int i1 = 0; i1 < posSize; i1++) {
					int pos = input.readInt();
					posList.add(pos);
				}
				items.add(new PostingListItem(id, posList, documentWordCount));
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
