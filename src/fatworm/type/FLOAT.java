package fatworm.type;

import java.math.BigDecimal;

import com.sun.org.apache.bcel.internal.generic.NEW;

import fatworm.files.MemoryBuffer;

public class FLOAT extends Field {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public float v;

	public FLOAT() {
		type = java.sql.Types.FLOAT;
	}

	public FLOAT(String x) {
		this();
		v = Float.valueOf(x);
		type = java.sql.Types.FLOAT;
	}
	
	public FLOAT(float x){
		this();
		v = x;
	}

	public FLOAT(double x) {
		this();
		v = (float) x;
	}

	public BigDecimal toDecimal() {
		return new BigDecimal(v).setScale(10, BigDecimal.ROUND_HALF_EVEN);
	}
	@Override
	public int compareTo(Object x){
		return toDecimal().compareTo(((Field)x).toDecimal());
	}

	@Override
	public void load(MemoryBuffer x) {
		v=x.getFloat();
	}

	@Override
	public void save(MemoryBuffer x) {
		x.putFloat(v);
	}

	@Override
	public int getByteSize() {
		return MemoryBuffer.SIZE_OF_FLOAT;
	}
	@Override
	public int hashCode() {
		return new Float(v).hashCode();
	}
	@Override
	public String toString(){
		return new Float(v).toString();
	}

	@Override
	public Object getValue() {
		return new Float(v);
	}
}
