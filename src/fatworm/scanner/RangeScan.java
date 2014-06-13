package fatworm.scanner;

import java.util.List;

import org.antlr.runtime.tree.Tree;

import fatworm.bplus.Index;
import fatworm.parser.FatwormLexer;
import fatworm.planner.Value;
import fatworm.type.Field;
import fatworm.util.Env;

public class RangeScan implements Scan {

	List<Field []> t;
	int maxiter, iter=-1;
	TupleSchema sch=null;
	Tuple nextTuple=null;
	MetaData metaData;
	String tbname;
	List<Integer> op;
	List<Value> v;
	Index sp;
	List<Integer> ret;
//	static int total=0;
	
	public RangeScan(String tablename, Tree tt, List<Integer> op, List<Value> v){
		//System.out.println(v.size());
		tbname=tablename;
		Table tb=Env.getByName(tablename);
		sch=TupleSchema.fromMeta(tb);
		int idx=sch.find(tt);
		metaData=tb.meta;
		maxiter=tb.size();
		t=tb.t;
		this.op=op;
		this.v=v;
		sp=tb.sp.get(idx);
		if(sp==null){
			sp=new Index();
			for(int i=0;i<t.size();++i)
				sp.insert(t.get(i)[idx], i);
			sp.finish();
			tb.sp.put(idx, sp);
		}
	}
	
	public boolean next() throws Exception {
		++iter;
		nextTuple=null;
		return iter<ret.size();
	}

	@Override
	public Object getObjectByIndex(int index) throws Exception {
		return getTuple().getColumn(index).getField();
	}

	@Override
	public void beforeFirst() throws Exception {
		Index tmp=sp.clone();
		for(int i=0;i<v.size();++i){
			int op=this.op.get(i);
			Field v=this.v.get(i).getField();
			switch(op){
			case FatwormLexer.T__114:tmp.findSmallerThan(v, false);break;
			case FatwormLexer.T__115:tmp.findSmallerThan(v, true);break;
			case FatwormLexer.T__118:tmp.findLargerThan(v, false);break;
			case FatwormLexer.T__119:tmp.findLargerThan(v, true);break;
			}
		}
		ret=tmp.iterator();
//		System.out.println(total+=ret.size());
	}

	@Override
	public int getColumnCount() throws Exception {
		return sch.ty.size();
	}

	@Override
	public Tuple getTuple() throws Exception {
		if(nextTuple!=null)return nextTuple;
		nextTuple=new Tuple();
		nextTuple.sch=getMeta();
		for(int i=0;i<metaData.type.size();++i)
			nextTuple.addColumn(new Column(tbname, metaData.type.get(0).name, t.get(this.ret.get(iter))[i]));
		return nextTuple;
	}

	@Override
	public TupleSchema getMeta() {
		return sch;
	}

}
