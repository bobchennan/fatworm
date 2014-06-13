package fatworm.type;

import java.math.BigDecimal;

import fatworm.files.MemoryBuffer;


public class BOOL extends Field {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public boolean v;
	public BOOL(){type = java.sql.Types.BOOLEAN;}
	public BOOL(boolean x){
		v = x;
		type = java.sql.Types.BOOLEAN;
	}
	public BOOL(String x) {
		v = (x.equalsIgnoreCase("false") || x.equals("0"))?false:true;
		type = java.sql.Types.BOOLEAN;
	}
	@Override
	public String toString() {
		return v ? "true" : "false";
	}
	@Override
	public int hashCode() {
		return v?1:0;
	}
	@Override
	public BigDecimal toDecimal() {
		return new BigDecimal(v?1:0).setScale(10);
	}
	@Override
	public int compareTo(Object x){
		return toDecimal().compareTo(((Field)x).toDecimal());
	}
	@Override
	public void load(MemoryBuffer x) {
		v=x.getBoolean();
	}
	@Override
	public void save(MemoryBuffer x) {
		x.putBoolean(v);
	}
	@Override
	public int getByteSize() {
		return MemoryBuffer.SIZE_OF_BOOLEAN;
	}
	@Override
	public Object getValue() {
		return v;
	}
}
