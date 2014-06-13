package fatworm.scanner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import fatworm.files.MemoryBuffer;
import fatworm.planner.AS;
import fatworm.planner.Expr;
import fatworm.planner.Value;
import fatworm.type.BOOL;
import fatworm.type.CHAR;
import fatworm.type.DATE;
import fatworm.type.DECIMAL;
import fatworm.type.FLOAT;
import fatworm.type.Field;
import fatworm.type.INT;
import fatworm.type.NULL;
import fatworm.type.TIMESTAMP;
import fatworm.type.VARCHAR;
import fatworm.util.Env;
import fatworm.util.Util;

public class Tuple {
	List<Column> cols;
	public TupleSchema sch;

	public Tuple() {
		cols = new ArrayList<Column>();
	}

	public Tuple(TupleSchema _sch){
		cols = new ArrayList<Column>();
		sch=_sch;
	}
	
	public void addColumn(Column x) {
		cols.add(x);
	}

	public Column getColumn(int x) {
		return cols.get(x);
	}

	public int getSize() {
		return cols.size();
	}

//	public int getByteSize() {
//		int ret = 0;
//		for (int i = 0; i < cols.size(); ++i)
//			if (cols.get(i).value instanceof NULL)
//				ret += 1;
//			else
//				ret+=1+cols.get(i).getByteSize();
//		return ret;
//	}

	public Column getColumn(String x){
//		if(sch==null)
//			System.out.println("cnx");
		int idx=sch.find(x);
		if(idx==-1)
			return null;
		else
			return cols.get(idx);
	}

	public void aliasTo(String s) {
		for (int i = 0; i < cols.size(); i++) {
			cols.get(i).setTableName(s);
		}
	}

	public Tuple match(List<Value> h) {
		Env.addCommand(this, sch);
		Tuple retTuple = new Tuple();
		for (int i = 0; i < h.size(); ++i) {
			if (h.get(i).calculate())
				retTuple.addColumn(new Column(cols.get(0).tableName, "", h.get(
						i).getField()));
			else {
				if (h.get(i).tt != null
						&& h.get(i).tt.getType() == fatworm.parser.FatwormLexer.AS|| h.get(i) instanceof AS){
					Field vField=null;
					if(h.get(i) instanceof AS){
						vField=h.get(i).getField();
						if(vField instanceof NULL)vField=Env.findColumn(((AS)h.get(i)).alias).getField();
					}
					retTuple.addColumn(new Column(cols.get(0).tableName, (h.get(i) instanceof AS)?((AS)h.get(i)).alias:h
							.get(i).tt.getChild(1).getText(),(h.get(i) instanceof AS)?vField:Util.eval(h.get(i).tt
							.getChild(0))));// (h.get(i) instanceof AS)?(h.get(i).tt!=null?h.get(i).tt.toStringTree():""):Env.getName(h.get(i).tt.getChild(0)), 
				}
				else
					retTuple.addColumn(new Column(cols.get(0).tableName, Env
							.getName(h.get(i).tt), h.get(i).getField()));
			}
		}
		Env.clearCommand();
		return retTuple;
	}

	public boolean match(Expr cond) {
		if (cond == null)
			return true;
		Env.addCommand(this, sch);
		boolean ret = Util.match(cond.t);
		Env.clearCommand();
		return ret;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Tuple))
			return false;

		Tuple z = (Tuple) o;

		if (cols.size() != z.cols.size())
			return false;
		for (int i = 0; i < cols.size(); ++i)
			if (!cols.get(i).equals(z.cols.get(i)))
				return false;
		return true;
	}

	@Override
	public int hashCode() {
		int ret = 0;
		for (int i = 0; i < cols.size(); ++i)
			ret ^= cols.get(i).hashCode();
		return ret;
	}

	@Override
	public String toString() {
		return cols.toString();
	}
}
