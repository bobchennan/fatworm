package fatworm.scanner;

import java.util.ArrayList;
import java.util.List;

import fatworm.type.Field;
import fatworm.util.Env;

public class TableScan implements Scan {

//	Table tb;
	List<Field []> t;
	int iter;
	int maxiter;
	TupleSchema sch=null;
	MetaData metaData;
	public String tbname;
	
	public TableScan(String tablename){
		tbname=tablename;
		Table tb=Env.getByName(tablename);
		sch=TupleSchema.fromMeta(tb);
		metaData=tb.meta;
		maxiter=tb.size();
		t=tb.t;
	}

	@Override
	public Tuple getTuple()throws Exception{
		Tuple ret=new Tuple();
		ret.sch=getMeta();
		for(int i=0;i<metaData.type.size();++i)
			ret.addColumn(new Column(tbname, metaData.type.get(0).name, t.get(iter)[i]));
		return ret;
	}
	
	@Override
	public boolean next() throws Exception {
		iter+=1;
		return iter<maxiter;
	}

	@Override
	public Object getObjectByIndex(int index) throws Exception {
		return t.get(iter)[index];
	}

	@Override
	public void beforeFirst() throws Exception {
		iter=-1;
	}

	@Override
	public int getColumnCount() throws Exception {
		return sch.ty.size();
	}

	@Override
	public TupleSchema getMeta() {
//		if(sch==null)
//			sch=TupleSchema.fromMeta(tb);
		return sch;
	}

}
