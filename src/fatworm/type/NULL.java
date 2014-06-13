package fatworm.type;

import fatworm.files.MemoryBuffer;

public class NULL extends Field {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static NULL instance;
	public NULL() {
		type = java.sql.Types.NULL;
	}
	
	public synchronized static Field getInstance(){
		if(instance == null)instance = new NULL();
		return instance;
	}
	
	@Override
	public int compareTo(Object x){
		return Integer.MAX_VALUE;
	}

	@Override
	public void load(MemoryBuffer x) {
		//nothing to do
	}

	@Override
	public void save(MemoryBuffer x) {
		//nothing to do
	}

	@Override
	public int getByteSize() {
		return 0;
	}
	@Override
	public String toString(){
		return "null";
	}

	@Override
	public Object getValue() {
		return null;
	}
}
