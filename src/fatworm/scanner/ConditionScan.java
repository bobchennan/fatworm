package fatworm.scanner;

import java.util.ArrayList;
import java.util.List;

import fatworm.planner.Expr;
import fatworm.util.Env;
import fatworm.util.Error;

public class ConditionScan implements Scan {

	public Scan lowScan;
	public List<Expr> cond=new ArrayList<Expr>();
	Tuple nextTuple;
	boolean next=false;
	
	public ConditionScan(Scan _lowScan, Expr _cond){
		cond.addAll(_cond.split());
		lowScan=_lowScan;
	}
	
	public ConditionScan(Scan _lowScan, List<Expr> _cond){
		cond.addAll(_cond);
		lowScan=_lowScan;
	}
	
	@Override
	public boolean next() throws Exception {
		if(next){next=false;return true;}
		nextTuple=null;
		return getTuple()!=null;
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
		while(lowScan.next()){
			nextTuple=lowScan.getTuple();
			nextTuple.sch=lowScan.getMeta();
			boolean ret=true;
			for(int i=0;i<cond.size();++i)
				if(!nextTuple.match(cond.get(i))){
					ret=false;
					break;
				}
			if(!ret)continue;
			return nextTuple;
		}
		nextTuple=null;
		return null;
	}

	@Override
	public TupleSchema getMeta(){
		return lowScan.getMeta();
	}

}
