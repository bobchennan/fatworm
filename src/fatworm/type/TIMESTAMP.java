package fatworm.type;

import java.sql.Timestamp;

import fatworm.files.MemoryBuffer;
import fatworm.util.Util;

public class TIMESTAMP extends Field {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6402476687174263908L;
	public java.sql.Timestamp v;

	public TIMESTAMP(){
		type = java.sql.Types.TIMESTAMP;
	}
	public TIMESTAMP(String x) {
		this();
		v = Util.parseTimestamp(x);
	}
	public TIMESTAMP(Timestamp timestamp) {
		this();
		v = timestamp;
	}
	public TIMESTAMP(Long x) {
		this();
		v = new java.sql.Timestamp(x);
	}
	@Override
	public String toString() {
		return v.toString();
	}
	@Override
	public int hashCode() {
		return v.hashCode();
	}
	@Override
	public int compareTo(Object x) {
		if(x instanceof CHAR)
			return compareTo(new TIMESTAMP(((CHAR)x).v));
		if(x instanceof VARCHAR)
			return compareTo(new TIMESTAMP(((VARCHAR)x).v));
		int ret = v.compareTo(((TIMESTAMP)x).v);
		return ret==0?0:(ret<0?-1:1);
	}
	@Override
	public void load(MemoryBuffer x) {
		v=new Timestamp(x.getLong());
	}
	@Override
	public void save(MemoryBuffer x) {
		x.putLong(v.getTime());
	}
	@Override
	public int getByteSize() {
		return MemoryBuffer.SIZE_OF_LONG;
	}
	@Override
	public Object getValue() {
		return v;
	}
}
