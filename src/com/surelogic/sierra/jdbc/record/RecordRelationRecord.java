package com.surelogic.sierra.jdbc.record;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Models a relation table between two entities in a database.
 * 
 * @author nathan
 * 
 * @param <R>
 * @param <S>
 */
public abstract class RecordRelationRecord<R extends Record<?>, S extends Record<?>>
		extends RelationRecord<R, S> {

	protected RecordRelationRecord(RecordMapper mapper) {
		super(mapper);
	}

}
