package fatworm.type;

import java.sql.Date;
import java.sql.Timestamp;

import fatworm.files.MemoryBuffer;
import fatworm.util.Util;

public class DATE extends Field {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6402476687174263908L;
	public Timestamp v;

	public DATE(){
		type = java.sql.Types.TIMESTAMP;
	}
	public DATE(String x) {
		this();
		v = Util.parseTimestamp(x);
	}
	public DATE(Timestamp DATE) {
		this();
		v = DATE;
	}
	public DATE(Long x) {
		this();
		v = new Timestamp(x);
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
			return compareTo(new DATE(((CHAR)x).v));
		if(x instanceof VARCHAR)
			return compareTo(new DATE(((VARCHAR)x).v));
		int ret = v.compareTo(((DATE)x).v);
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
