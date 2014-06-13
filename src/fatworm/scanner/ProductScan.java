package fatworm.scanner;

import java.util.ArrayList;
import java.util.List;

import fatworm.type.Field;
import fatworm.util.Env;
import fatworm.util.Error;
import fatworm.util.Util;

public class ProductScan implements Scan {
	
	public Scan sa;
	public Scan sb;
	Tuple nextTuple;
	TupleSchema sch=null;
	ArrayList<Tuple> va,vb;
	int ida,idb;
	
	public ProductScan(Scan a, Scan b){
		sa=a;
		sb=b;
	}
	
	@Override
	public boolean next() throws Exception {
		nextTuple=null;
		++idb;
		if(idb>=vb.size()){++ida;idb=0;}
		if(ida>=va.size())return false;
		return true;
	}

	@Override
	public Object getObjectByIndex(int index) throws Exception {
		return getTuple().getColumn(index);
	}

	@Override
	public void beforeFirst() throws Exception {
		getMeta();
		sa.beforeFirst();
		sa.next();
		Tuple t=sa.getTuple();
		if(t==null)return;
		int cnt1=t.getSize();
		
		sb.beforeFirst();
		sb.next();
		t=sb.getTuple();
		if(t==null)return;
		va=new ArrayList<Tuple> ();
		do{
			va.add(sa.getTuple());
		}while(sa.next());
		vb=new ArrayList<Tuple> ();
		do{
			vb.add(sb.getTuple());
		}while(sb.next());
		
		sa=null;//for gc
		sb=null;//for gc
		ida=0;
		idb=-1;
	}

	@Override
	public int getColumnCount() throws Exception {
		return sch.ty.size();
	}

	@Override
	public Tuple getTuple() throws Exception {
		if(nextTuple!=null)return nextTuple;
		Tuple ret=new Tuple(sch);
		Tuple s1=va.get(ida);
		Tuple s2=vb.get(idb);
		ret.cols.addAll(s1.cols);
		ret.cols.addAll(s2.cols);
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
//package fatworm.scanner;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import fatworm.type.Field;
//import fatworm.util.Env;
//import fatworm.util.Error;
//import fatworm.util.Util;
//
//public class ProductScan implements Scan {
//	
//	public Scan sa;
//	public Scan sb;
//	Tuple nextTuple;
//	TupleSchema sch=null;
//	ArrayList<Field []> va,vb;
//	String[] columnName, tableName;
//	int ida,idb;
//	
//	public ProductScan(Scan a, Scan b){
//		sa=a;
//		sb=b;
//	}
//	
//	@Override
//	public boolean next() throws Exception {
//		nextTuple=null;
//		++idb;
//		if(idb>=vb.size()){++ida;idb=0;}
//		if(ida>=va.size())return false;
//		return true;
//	}
//
//	@Override
//	public Object getObjectByIndex(int index) throws Exception {
//		return getTuple().getColumn(index);
//	}
//
//	@Override
//	public void beforeFirst() throws Exception {
//		getMeta();
//		columnName=new String[sch.ty.size()];
//		tableName=new String[sch.ty.size()];
//		sa.beforeFirst();
//		sa.next();
//		Tuple t=sa.getTuple();
//		if(t==null)return;
//		for(int i=0;i<t.getSize();++i){
//			columnName[i]=t.getColumn(i).columnName;
//			tableName[i]=t.getColumn(i).tableName;
//		}
//		int cnt1=t.getSize();
//		
//		sb.beforeFirst();
//		sb.next();
//		t=sb.getTuple();
//		if(t==null)return;
//		for(int i=0;i<t.getSize();++i){
//			columnName[i+cnt1]=t.getColumn(i).columnName;
//			tableName[i+cnt1]=t.getColumn(i).tableName;
//		}
//		va=new ArrayList<Field[]> ();
//		do{
//			va.add(Util.getFieldArray(sa.getTuple()));
//		}while(sa.next());
//		vb=new ArrayList<Field[]> ();
//		do{
//			vb.add(Util.getFieldArray(sb.getTuple()));
//		}while(sb.next());
//		
//		sa=null;//for gc
//		sb=null;//for gc
//		ida=0;
//		idb=-1;
//	}
//
//	@Override
//	public int getColumnCount() throws Exception {
//		return sch.ty.size();
//	}
//
//	@Override
//	public Tuple getTuple() throws Exception {
//		if(nextTuple!=null)return nextTuple;
//		Tuple ret=new Tuple(sch);
//		Field[] s1=va.get(ida);
//		Field[] s2=vb.get(idb);
//		for(int i=0;i<s1.length;++i)
//			ret.addColumn(new Column(tableName[i],columnName[i],s1[i]));
//		for(int i=0;i<s2.length;++i)
//			ret.addColumn(new Column(tableName[i+s1.length],columnName[i+s1.length],s2[i]));
//		nextTuple=ret;
//		return ret;
//	}
//
//	@Override
//	public TupleSchema getMeta(){
//		if(sch!=null)return sch;
//		TupleSchema scha=sa.getMeta();
//		TupleSchema schb=sb.getMeta();
//		sch=TupleSchema.merge(scha,schb);
//		return sch;
//	}
//
//}