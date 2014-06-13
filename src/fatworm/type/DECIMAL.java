package fatworm.type;

import java.math.BigDecimal;

import fatworm.files.MemoryBuffer;

public class DECIMAL extends Field{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public BigDecimal v;
	public DECIMAL(String x) {
		this();
		//System.out.println(x);
		v = new BigDecimal(x).setScale(10, BigDecimal.ROUND_HALF_EVEN);
		type = java.sql.Types.DECIMAL;
	}
	public DECIMAL() {
		type = java.sql.Types.DECIMAL;
	}
	public DECIMAL(BigDecimal x){
		this();
		v = x.setScale(10, BigDecimal.ROUND_HALF_EVEN);
		type = java.sql.Types.DECIMAL;
	}

	public DECIMAL(int v2) {
		v=BigDecimal.valueOf(v2);
	}
	@Override
	public BigDecimal toDecimal(){
		return v;
	}
	@Override
	public int compareTo(Object x){
		return toDecimal().compareTo(((Field)x).toDecimal());
	}
	@Override
	public void load(MemoryBuffer x) {
		v=new BigDecimal(String.copyValueOf(x.getCharArray()));
	}
	@Override
	public void save(MemoryBuffer x) {
		x.putCharArray(v.toString().toCharArray());
	}
	@Override
	public int getByteSize() {
		return (v.toString().toCharArray().length<<1)+MemoryBuffer.SIZE_OF_INT;
	}
	@Override
	public int hashCode() {
		return v.hashCode();
	}
	@Override
	public String toString(){
		return v.toString();
	}
	@Override
	public Object getValue() {
		return v;
	}
}
