package fatworm.scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

import fatworm.parser.FatwormLexer;
import fatworm.planner.ColumnName;
import fatworm.planner.Expr;
import fatworm.planner.SelectPlan;
import fatworm.planner.Transfer;
import fatworm.type.Field;
import fatworm.type.INT;
import fatworm.util.Env;
import fatworm.util.Error;

public class TupleSchema {
	public List<Integer> ty;
	public Map<ColumnName, Integer> h;

	public TupleSchema() {
		ty = new ArrayList<Integer>();
		h = new HashMap<ColumnName, Integer>();
	}

	public List<Integer> getType() {
		return ty;
	}
	
	public void getFrom(TupleSchema tmp, int idx, int v){
//		ty.add(v);
		Iterator it = tmp.h.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			ColumnName col = (ColumnName) pairs.getKey();
			int pos = (Integer)pairs.getValue();
			if(pos==idx)
				h.put(col, ty.size()-1);
		}
	}
	
	public void add(String table, String col, int v) {
		ty.add(v);
		h.put(new ColumnName(table, col), ty.size() - 1);
		if (!h.containsKey(new ColumnName("", col)))
			h.put(new ColumnName("", col), ty.size() - 1);
	}

	public void add(String table, String col) {
		h.put(new ColumnName(table, col), ty.size()-1);
		if (!h.containsKey(new ColumnName("", col)))
			h.put(new ColumnName("", col), ty.size()-1);
	}
	
	public String lastTable=null;
	public String lastCol=null;
	public int lastRet=-1;
	
	public int find(String table, String col){
		if(table.equals(lastTable)&&col.equals(lastCol))
			if(lastRet!=-1)
				return lastRet;
		if(h.containsKey(new ColumnName(table,col))){
			lastRet=h.get(new ColumnName(table,col));
			lastTable=table;
			lastCol=col;
			return lastRet;
		}
		else
			return -1;
	}
	
	public int find(Tree t){
		if(t.getText().equals("."))
			return find(t.getChild(0).getText(),t.getChild(1).getText());
		else
			return find(t.toStringTree());
	}

	@Override
	public String toString() {
		return ty.toString() + h.toString();
	}
	
	public int getSize(){
		return ty.size();
	}
	
	public static TupleSchema merge(TupleSchema x, TupleSchema y){
		TupleSchema ret=new TupleSchema();
		Iterator it = x.h.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			ColumnName col = (ColumnName) pairs.getKey();
			int pos = (Integer)pairs.getValue();
			ret.h.put(col, pos);
		}
		ret.ty.addAll(x.ty);
		
		it = y.h.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			ColumnName col = (ColumnName) pairs.getKey();
			int pos = (Integer)pairs.getValue();
			ret.h.put(col, pos+x.getSize());
		}
		ret.ty.addAll(y.ty);
		return ret;
	}
	
	public static TupleSchema rename(TupleSchema sch, String table){
		TupleSchema ret=new TupleSchema();
		//ret.h.putAll(sch.h);
		Iterator it = sch.h.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			ColumnName col = (ColumnName) pairs.getKey();
			int pos = (Integer)pairs.getValue();
			ret.h.put(new ColumnName(table,col.col), pos);
			ret.h.put(new ColumnName("",col.col),pos);
		}
		ret.ty.addAll(sch.ty);
		return ret;
	}

	public int find(String x) {
		if(x.contains("."))
			return find(x.substring(0,x.indexOf(".")),x.substring(x.indexOf(".")+1));
		else
			return find("",x);
	}
	
	public static TupleSchema fromMeta(Table tb){
		TupleSchema sch=new TupleSchema();
		if(tb.meta!=null&&tb.meta.type!=null)
		for(int i=0;i<tb.meta.type.size();++i)
			sch.add(tb.tbname,tb.meta.type.get(i).name,Env.fromFieldtoType(tb.meta.type.get(i).type));
		return sch;
	}

	public boolean canExecute(Tree y) {
		if(y==null)return true;
		switch(y.getType()){
		case fatworm.parser.FatwormLexer.T__118:
		case fatworm.parser.FatwormLexer.T__114:
		case fatworm.parser.FatwormLexer.T__119:
		case fatworm.parser.FatwormLexer.T__115:
		case fatworm.parser.FatwormLexer.T__117:
		case fatworm.parser.FatwormLexer.T__116:
		case fatworm.parser.FatwormLexer.OR:
		case fatworm.parser.FatwormLexer.AND:
		case fatworm.parser.FatwormLexer.T__109:
		case fatworm.parser.FatwormLexer.T__111:
		case fatworm.parser.FatwormLexer.T__108:
		case fatworm.parser.FatwormLexer.T__113:
		case fatworm.parser.FatwormLexer.T__105:
			return canExecute(y.getChild(0)) && canExecute(y.getChild(1));
		case fatworm.parser.FatwormLexer.EXISTS:
		case fatworm.parser.FatwormLexer.NOT_EXISTS:
		case fatworm.parser.FatwormLexer.IN:
		case fatworm.parser.FatwormLexer.ANY:
		case fatworm.parser.FatwormLexer.ALL:
		case FatwormLexer.SELECT:
		case FatwormLexer.SELECT_DISTINCT:
			return false;
		case fatworm.parser.FatwormLexer.STRING_LITERAL:
		case fatworm.parser.FatwormLexer.INTEGER_LITERAL:
		case fatworm.parser.FatwormLexer.FLOAT_LITERAL:
		case fatworm.parser.FatwormLexer.TRUE:
		case fatworm.parser.FatwormLexer.FALSE:
			return true;
		case fatworm.parser.FatwormLexer.T__112:
			return h.containsKey(new ColumnName(y.getChild(0).getText(), y.getChild(1).getText()));
		default:
			return h.containsKey(new ColumnName(y.toStringTree()));
		}
	}
}
