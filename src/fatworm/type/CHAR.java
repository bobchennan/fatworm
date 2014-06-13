package fatworm.type;

import fatworm.files.MemoryBuffer;


public class CHAR extends Field {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String v;
	public int len;
	public CHAR(){
		type = java.sql.Types.CHAR;
	}
	public CHAR(int len){
		this.len=len;
		type = java.sql.Types.CHAR;
	}
	public CHAR(String s) {
		this();
		v = (((s.startsWith("'") && s.endsWith("'"))||(s.startsWith("\"") && s.endsWith("\""))) && s.length() >= 2)?s.substring(1,s.length()-1):s;
		type = java.sql.Types.CHAR;
	}
	@Override
	public int compareTo(Object x){
		int ret=new String("'"+v+"'").compareToIgnoreCase(x.toString());
		return ret!=0?(ret>0?1:-1):0;
	}
	@Override
	public void load(MemoryBuffer x) {
		v=String.copyValueOf(x.getCharArray());
	}
	@Override
	public void save(MemoryBuffer x) {
		x.putCharArray(v.toCharArray());
	}
	@Override
	public int getByteSize() {
		return MemoryBuffer.SIZE_OF_INT+(v.toCharArray().length<<1);
	}
	@Override
	public String toString(){
		return "'"+v+"'";
	}
	@Override
	public int hashCode() {
		return v.hashCode();
	}
	@Override
	public Object getValue() {
		return v;
	}
}
