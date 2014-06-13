package fatworm.scanner;

import java.util.List;

import fatworm.type.NULL;

public class ConstantScan implements Scan {

	boolean isfirst=false;
	
	@Override
	public boolean next() throws Exception {
		if(isfirst){isfirst=false;return true;}
		else return false;
	}

	@Override
	public Object getObjectByIndex(int index) throws Exception {
		return new NULL();
	}

	@Override
	public void beforeFirst() throws Exception {
		//System.out.println("cnx");
		isfirst=true;
	}

	@Override
	public int getColumnCount() throws Exception {
		return 1;
	}

	@Override
	public Tuple getTuple() throws Exception {
		Tuple ret = new Tuple(new TupleSchema());
		ret.addColumn(new Column("", "", new NULL()));
		return ret;
	}

	@Override
	public TupleSchema getMeta() {
		return new TupleSchema();
	}

}
