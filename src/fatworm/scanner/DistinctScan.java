package fatworm.scanner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fatworm.type.Field;
import fatworm.util.Env;
import fatworm.util.Error;

public class DistinctScan implements Scan {
	
	public Scan lowScan;
	Tuple nextTuple, lastTuple;
	Set<List<Field>> h;
	boolean isfirst=false;
	
	public DistinctScan(Scan _low){
		nextTuple=null;
		lastTuple=null;
		lowScan=_low;
		if(!(_low instanceof SortScan))
			h=new HashSet<List<Field>>();
	}

	@Override
	public boolean next() throws Exception {
		if(isfirst){isfirst=false;return true;}
		lastTuple=nextTuple;
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

	public List<Field> toList(Tuple x){
		List<Field> ret=new ArrayList<Field>();
		for(int i=0;i<x.getSize();++i)
			ret.add(x.getColumn(i).getField());
		return ret;
	}
	
	@Override
	public Tuple getTuple() throws Exception {
		if(nextTuple!=null)return nextTuple;
		if(lowScan instanceof SortScan){
			while(lowScan.next()){
				if (lastTuple==null||!toList(lastTuple).equals(toList(lowScan.getTuple()))){
					nextTuple=lowScan.getTuple();
					break;
				}
			}
		}
		else{
			while(lowScan.next()){
				Tuple tmp=lowScan.getTuple();
				if(!h.contains(toList(tmp))){
					nextTuple=tmp;
					h.add(toList(tmp));
					break;
				}
			}
		}
		return nextTuple;
	}

	@Override
	public TupleSchema getMeta(){
		return lowScan.getMeta();
	}

}
