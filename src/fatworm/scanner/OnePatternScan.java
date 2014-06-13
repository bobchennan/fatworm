package fatworm.scanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.antlr.misc.MultiMap;

import fatworm.planner.Expr;
import fatworm.type.Field;
import fatworm.util.Env;
import fatworm.util.Error;
import fatworm.util.Util;

public class OnePatternScan implements Scan {
	
	public Scan sa;
	public Scan sb;
	int splitCond;
	Tuple nextTuple;
	TupleSchema sch=null;
	ArrayList<Field []> va,vb;
	String[] columnName,tableName;
	int ida,idb,p1,p2;
	List<Expr> cond;
	MultiMap<Field, Integer> h=new MultiMap<Field, Integer>();
	
	List<Integer> now=null;
	
	public OnePatternScan(Scan a, Scan b, List<Expr> _cond, int splitCond){
		sa=a;
		sb=b;
		cond=_cond;
		this.splitCond=splitCond;
	}
	
	@Override
	public boolean next() throws Exception {
		while(true){
			nextTuple=null;
			++idb;
			if(idb==now.size()){
				++ida;
				if(ida>=va.size())return false;
				now=h.get(va.get(ida)[p1]);
				idb=0;
			}
			Tuple ret=getTuple();
			boolean found=true;
			ret.sch=sch;
			for(int i=0;i<cond.size();++i)
				if(!ret.match(cond.get(i))){
					found=false;
					break;
				}
			if(found)
				return true;
		}
	}

	@Override
	public Object getObjectByIndex(int index) throws Exception {
		return getTuple().getColumn(index);
	}

	@Override
	public void beforeFirst() throws Exception {
		getMeta();
		columnName=new String[sch.ty.size()];
		tableName=new String[sch.ty.size()];
		sa.beforeFirst();
		sa.next();
		Tuple t=sa.getTuple();
		for(int i=0;i<t.getSize();++i){
			columnName[i]=t.getColumn(i).columnName;
			tableName[i]=t.getColumn(i).tableName;
		}
		int cnt1=t.getSize();
		
		sb.beforeFirst();
		sb.next();
		t=sb.getTuple();
		for(int i=0;i<t.getSize();++i){
			columnName[i+cnt1]=t.getColumn(i).columnName;
			tableName[i+cnt1]=t.getColumn(i).tableName;
		}
		
		for(int i=0;i<cond.size();++i)
			if(i==splitCond){
				p1=sa.getMeta().find(cond.get(i).t.getChild(0));
				if(p1!=-1)
					p2=sb.getMeta().find(cond.get(i).t.getChild(1));
				if(p1==-1||p2==-1){
					p2=sb.getMeta().find(cond.get(i).t.getChild(0));
					p1=sa.getMeta().find(cond.get(i).t.getChild(1));
				}
				cond.remove(i);
				break;
			}
		
		vb=new ArrayList<Field[]> ();
		do{
			Field[] add=Util.getFieldArray(sb.getTuple());
			vb.add(add);
			if(!h.containsKey(add[p2]))
				h.put(add[p2], new ArrayList<Integer>());
			h.get(add[p2]).add(vb.size()-1);
		}while(sb.next());
		va=new ArrayList<Field[]> ();
		do{
			Field[] add=Util.getFieldArray(sa.getTuple());
			if(h.containsKey(add[p1]))
				va.add(add);
		}while(sa.next());
		
		ida=0;
		now=h.get(va.get(ida)[p1]);
		idb=-1;
		sa=null;//for gc
		sb=null;//for gc
	}

	@Override
	public int getColumnCount() throws Exception {
		return sch.ty.size();
	}

	@Override
	public Tuple getTuple() throws Exception {
		if(nextTuple!=null)return nextTuple;
		Tuple ret=new Tuple(sch);
		Field[] s1=va.get(ida);
		Field[] s2=vb.get(now.get(idb));
		for(int i=0;i<s1.length;++i)
			ret.addColumn(new Column(tableName[i],columnName[i],s1[i]));
		for(int i=0;i<s2.length;++i)
			ret.addColumn(new Column(tableName[i+s1.length],columnName[i+s1.length],s2[i]));
		nextTuple=ret;
		return ret;
	}

	@Override
	public TupleSchema getMeta(){
		if(sch!=null)return sch;
		TupleSchema scha=sa.getMeta();
		TupleSchema schb=sb.getMeta();
		sch=TupleSchema.merge(scha,schb);
		return sch;
	}

}
