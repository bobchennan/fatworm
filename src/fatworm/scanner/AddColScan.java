package fatworm.scanner;

import java.util.List;

import fatworm.planner.Value;

public class AddColScan implements Scan {

	public Scan lowScan;
	public List<Value> toadd;
	
	public AddColScan(Scan lowScan, List<Value> toadd){
		this.lowScan=lowScan;
		this.toadd=toadd;
	}
	
	@Override
	public boolean next() throws Exception {
		return lowScan.next();
	}

	@Override
	public Object getObjectByIndex(int index) throws Exception {
		return getTuple().getColumn(index).getField();
	}

	@Override
	public void beforeFirst() throws Exception {
		lowScan.beforeFirst();
	}

	@Override
	public int getColumnCount() throws Exception {
		return lowScan.getColumnCount()+toadd.size();
	}

	@Override
	public Tuple getTuple() throws Exception {
		Tuple nextTuple=lowScan.getTuple();
		for(int i=0;i<toadd.size();++i)
			nextTuple.addColumn(new Column("", "", toadd.get(i).getField()));
		return nextTuple;
	}

	@Override
	public TupleSchema getMeta() {
		TupleSchema schema=lowScan.getMeta();
		for(int i=0;i<toadd.size();++i)
			schema.add("", "", toadd.get(i).getType());
		return schema;
	}

}
