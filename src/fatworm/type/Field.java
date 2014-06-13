package fatworm.type;

import java.io.Serializable;
import java.math.BigDecimal;

import fatworm.files.MemoryBuffer;

public abstract class Field implements Serializable, java.lang.Comparable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int type;
	public Field() {
	}

	// FIXME watch & check this!!! Cause equals is used in HashMap besides hash code!!!
	public boolean equals(Object o){
		if(o == null)return false;
		return this.toString().equals(o.toString());
	}
	
	public static Field fromString(int type, String x){
		if(x.equalsIgnoreCase("null"))return NULL.getInstance();
		switch(type){
		case java.sql.Types.BOOLEAN:
			return new BOOL(x);
		case java.sql.Types.CHAR:
//			Util.warn("I just constructed CHAR field " + new String(x));
			return new CHAR(new String(x));
		case java.sql.Types.DECIMAL:
			return new DECIMAL(x);
		case java.sql.Types.FLOAT:
			return new FLOAT(x);
		case java.sql.Types.INTEGER:
			return new INT(x);
		case java.sql.Types.NULL:
			return NULL.getInstance();
		case java.sql.Types.VARCHAR:
//			Util.warn("I just constructed VARCHAR field " + new String(x));
			return new VARCHAR(new String(x));
			
			default:
				return null;
		}
	}
	public abstract int compareTo(Object x);
	public BigDecimal toDecimal(){
		return null;
	}
	public abstract void load(MemoryBuffer x);
	public abstract void save(MemoryBuffer x);
	public abstract int getByteSize();
	@Override
	public abstract String toString();
	public abstract Object getValue();
}
