package fatworm.type;

import java.math.BigDecimal;
import java.nio.ByteBuffer;

import fatworm.files.MemoryBuffer;
import fatworm.util.Util;

public class INT extends Field {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int v;
	public INT(){
		this.type = java.sql.Types.INTEGER;
	}
	public INT(int v) {
		this();
		this.v = v;
	}
	
	public INT(ByteBuffer b){
		this();
		v = b.getInt();
	}

	public INT(String x) {
		this();
//		v = Integer.valueOf(Util.trim(x));
		v = new BigDecimal(x).intValueExact();
	}

	public BigDecimal toDecimal() {
		return new BigDecimal(v).setScale(10);
	}
	@Override 
	public String toString(){
		return Integer.toString(v);
	}
	@Override
	public int compareTo(Object x){
		if(!(x instanceof INT))
			x=new FLOAT(Float.parseFloat(Util.trim(x.toString())));
		return toDecimal().compareTo(((Field)x).toDecimal());
	}
	@Override
	public void load(MemoryBuffer x) {
		v=x.getInt();
	}
	@Override
	public void save(MemoryBuffer x) {
		x.putInt(v);
	}
	@Override
	public int getByteSize() {
		return MemoryBuffer.SIZE_OF_INT;
	}
	@Override
	public int hashCode() {
		return new Integer(v).hashCode();
	}
	@Override
	public Object getValue() {
		return v;
	}
}
