package com.surelogic.sierra.gwt.client.data;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class ImportanceView_CustomFieldSerializer {
	public static void deserialize(SerializationStreamReader reader,
			ImportanceView instance) throws SerializationException {
		// nothing to do
	}

	public static ImportanceView instantiate(SerializationStreamReader reader)
			throws SerializationException {
		try {
			return ImportanceView.fromString(reader.readString());
		} catch (final IllegalArgumentException e) {
			throw new SerializationException(e);
		}
	}

	public static void serialize(SerializationStreamWriter writer,
			ImportanceView instance) throws SerializationException {
		writer.writeString(instance.getName());
	}

	private ImportanceView_CustomFieldSerializer() {
	}
}
