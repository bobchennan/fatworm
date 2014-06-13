package fatworm.scanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fatworm.driver.Statement;
import fatworm.planner.ColumnName;
import fatworm.type.Field;
import fatworm.util.Env;

public class SortScan implements Scan {
	
	public Scan lowScan;
	public List<ColumnName> col;
	public List<Boolean> ord;
	List<Tuple> results;
	int iter;
	TupleSchema sch;
	
	public SortScan(Scan _low, List<ColumnName> _col, List<Boolean> _ord){
		lowScan=_low;
		col=_col;
		ord=_ord;
	}

	@Override
	public boolean next() throws Exception {
		++iter;
		return iter<results.size();
	}

	@Override
	public Object getObjectByIndex(int index) throws Exception {
		return getTuple().getColumn(index);
	}

	@Override
	public void beforeFirst() throws Exception {
		lowScan.beforeFirst();
		results=new ArrayList<Tuple>();
		while(lowScan.next())
			results.add(lowScan.getTuple());
		
		final int[] pos=new int[col.size()];
		getMeta();
		for(int i=0;i<col.size();++i){
			pos[i]=sch.find(col.get(i).toString());
			if(pos[i]==-1)pos[i]=sch.find(Statement.aliasTable.get(col.get(i).toString()));
		}
		//System.out.println("order "+pos);
		Collections.sort(results, new Comparator<Tuple>(){
			public int compare(Tuple a, Tuple b){
				for(int i=0;i<col.size();++i){
					Field aField=a.getColumn(pos[i]).value;
					Field bField=b.getColumn(pos[i]).value;
					int ret=aField.compareTo(bField);
					if(ret==0)continue;
					else return ord.get(i)?(-ret):(ret);
				}
				for(int i=0;i<a.getSize()&&i<b.getSize();++i){
					Field aField=a.getColumn(i).value;
					Field bField=b.getColumn(i).value;
					int ret=aField.compareTo(bField);
					if(ret==0)continue;
					else return ret;
				}
				return -1;
			}
		});
		iter=-1;
	}

	@Override
	public int getColumnCount() throws Exception {
		return lowScan.getColumnCount();
	}

	@Override
	public Tuple getTuple() throws Exception {
		return results.get(iter);
	}

	@Override
	public TupleSchema getMeta() {
		sch=lowScan.getMeta();
		return sch;
	}

}
