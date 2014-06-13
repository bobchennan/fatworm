package fatworm.scanner;

import java.util.List;

import fatworm.util.Env;

public class AsScan implements Scan {
	
	public Scan lowScan;
	public String alias;
	Tuple nextTuple;
	boolean next=false;
	public TupleSchema sch=null;
	
	public AsScan(Scan _lowScan, String _alias){
		lowScan=_lowScan;
		alias=_alias;
	}

	@Override
	public boolean next() throws Exception {
		if(next){next=false;return true;}
		nextTuple=null;
		return lowScan.next();
	}

	@Override
	public Object getObjectByIndex(int index) throws Exception {
		return getTuple().getColumn(index);
	}

	@Override
	public void beforeFirst() throws Exception {
		lowScan.beforeFirst();
	}

	@Override
	public int getColumnCount() throws Exception {
		return lowScan.getColumnCount();
	}

	@Override
	public Tuple getTuple() throws Exception {
		if(nextTuple!=null)return nextTuple;
//		Tuple ret=new Tuple(lowScan.getMeta());
//		Tuple son=lowScan.getTuple();
//		for(int i=0;i<son.getSize();++i){
//			Column tmp=son.getColumn(i).clone();
//			tmp.setTableName(alias);
//			ret.addColumn(tmp);
//		}
//		nextTuple=ret;
//		return ret;
		nextTuple=lowScan.getTuple();
		nextTuple.sch=getMeta();
		return nextTuple;
	}

	@Override
	public TupleSchema getMeta(){
		if(sch==null)sch=TupleSchema.rename(lowScan.getMeta(),alias);
		return sch;
	}

}
