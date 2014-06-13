package fatworm.util;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

import com.sun.org.apache.bcel.internal.generic.RETURN;

import fatworm.files.MemoryBuffer;
import fatworm.parser.FatwormLexer;
import fatworm.planner.SelectPlan;
import fatworm.planner.Transfer;
import fatworm.planner.Value;
import fatworm.scanner.ColumnSchema;
import fatworm.scanner.Scan;
import fatworm.scanner.Tuple;
import fatworm.type.CHAR;
import fatworm.type.DATE;
import fatworm.type.FLOAT;
import fatworm.type.Field;
import fatworm.type.INT;
import fatworm.type.NULL;
import fatworm.type.TIMESTAMP;
import fatworm.type.VARCHAR;

public class Util {
	
	public static String lastsubquery=null;
	public static Set<Field> inquery=new HashSet<Field>();
	
	public static String trim(String s){
		if(((s.startsWith("'") && s.endsWith("'"))||(s.startsWith("\"") && s.endsWith("\""))) && s.length() >= 2)
			return s.substring(1,s.length()-1);
		else 
			return s;
	}
	
	public static boolean contains(Tree t, String x){
		if(t.getText().equals(x)||t.toStringTree().equals(x))
			return true;
		for(int i=0;i<t.getChildCount();++i)
			if(contains(t.getChild(i),x))
				return true;
		return false;
	}
	
	public static java.sql.Timestamp parseTimestamp(String x) {
		try {
			return new java.sql.Timestamp(Long.valueOf(x));
		} catch (NumberFormatException e) {
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd hh:mm:ss");
			try {
				return new java.sql.Timestamp(format.parse(x).getTime());
			} catch (ParseException ee) {
				fatworm.util.Error.print(ee);
			}
		} catch (Throwable e) {
			Error.print(e);
		}
		fatworm.util.Error.print("Timestamp from " + x + " failed!");
		return null;
	}

	public static Field trim(Field v1, Field v2) {
		if (v1 instanceof CHAR && v2 instanceof VARCHAR)
			v1 = new VARCHAR(((CHAR) v1).v);
		if (v1 instanceof CHAR)
			return new CHAR(((CHAR) v1).v.substring(0,
					Math.min(((CHAR) v2).len, ((CHAR) v1).v.length())));
		if (v1 instanceof VARCHAR)
			return new VARCHAR(((VARCHAR) v1).v.substring(0,
					Math.min(((VARCHAR) v2).len, ((VARCHAR) v1).v.length())));
		return new NULL();
	}

	public static boolean compare(Field one, Field two, int op) {
		if(one instanceof NULL)return false;
		if(two instanceof NULL)return false;
		switch (op) {
		case fatworm.parser.FatwormLexer.T__118:
			return one.compareTo(two) == 1;
		case fatworm.parser.FatwormLexer.T__114:
			return one.compareTo(two) == -1;
		case fatworm.parser.FatwormLexer.T__119:
			return one.compareTo(two) != -1;
		case fatworm.parser.FatwormLexer.T__115:
			return one.compareTo(two) != 1;
		case fatworm.parser.FatwormLexer.T__117:
			return one.compareTo(two) == 0;
		case fatworm.parser.FatwormLexer.T__116:
			return one.compareTo(two) != 0;
		default:
			return false;
		}
	}

	public static boolean match(Tree y) {
		switch (y.getType()) {
		case fatworm.parser.FatwormLexer.OR:
			return match(y.getChild(0)) || match(y.getChild(1));
		case fatworm.parser.FatwormLexer.AND:
			return match(y.getChild(0)) && match(y.getChild(1));
		case fatworm.parser.FatwormLexer.T__118:
		case fatworm.parser.FatwormLexer.T__114:
		case fatworm.parser.FatwormLexer.T__119:
		case fatworm.parser.FatwormLexer.T__115:
		case fatworm.parser.FatwormLexer.T__117:
		case fatworm.parser.FatwormLexer.T__116:
			return compare(eval(y.getChild(0)), eval(y.getChild(1)), y.getType());
		case fatworm.parser.FatwormLexer.EXISTS:
		case fatworm.parser.FatwormLexer.NOT_EXISTS:
			SelectPlan yPlan = (SelectPlan) new Transfer().DFS((CommonTree) y
					.getChild(0));
			Scan yScan = yPlan.toScan();
			try {
				yScan.getMeta();
				yScan.beforeFirst();
				return y.getType() == fatworm.parser.FatwormLexer.EXISTS ? yScan
						.next() : !yScan.next();
			} catch (Throwable e) {
				Error.print(e);
				return false;
			}
		case fatworm.parser.FatwormLexer.IN:
			yPlan = (SelectPlan) new Transfer().DFS((CommonTree) y.getChild(1));
			boolean subqueryop=false;
			if(((SelectPlan)yPlan).where==null)subqueryop=true;
			if(subqueryop){
				Field vField = eval(y.getChild(0));
				if(lastsubquery==null||!lastsubquery.equals(y.toStringTree())){
					lastsubquery=y.toStringTree();
					yScan = yPlan.toScan();
					try {
						yScan.getMeta();
						yScan.beforeFirst();
						while (yScan.next()) {
							Tuple tp = yScan.getTuple();
							inquery.add(tp.getColumn(0).getField());
						}
					} catch (Throwable e) {
						Error.print(e);
					}
				}
				return inquery.contains(vField);
			}
			yScan = yPlan.toScan();
			Field vField = eval(y.getChild(0));
			try {
				yScan.getMeta();
				yScan.beforeFirst();
				while (yScan.next()) {
					Tuple tp = yScan.getTuple();
					if (tp.getColumn(0).getField().equals(vField))
						return true;
				}
				return false;
			} catch (Throwable e) {
				Error.print(e);
				return false;
			}
		case fatworm.parser.FatwormLexer.ANY:
			yPlan = (SelectPlan) new Transfer().DFS((CommonTree) y.getChild(2));
			yScan = yPlan.toScan();
			vField = eval(y.getChild(0));
			try {
				yScan.getMeta();
				yScan.beforeFirst();
				while (yScan.next()) {
					Tuple tp = yScan.getTuple();
					if (compare(vField, tp.getColumn(0).getField(),
							y.getChild(1).getType()))
						return true;
				}
				return false;
			} catch (Throwable e) {
				Error.print(e);
				return false;
			}
		case fatworm.parser.FatwormLexer.ALL:
			yPlan = (SelectPlan) new Transfer().DFS((CommonTree) y.getChild(2));
			yScan = yPlan.toScan();
			vField = eval(y.getChild(0));
			try {
				yScan.getMeta();
				yScan.beforeFirst();
				while (yScan.next()) {
					Tuple tp = yScan.getTuple();
					if (!compare(vField, tp.getColumn(0).getField(), y
							.getChild(1).getType()))
						return false;
				}
				return true;
			} catch (Throwable e) {
				Error.print(e);
				return false;
			}
			// TODO MAYBE NEED TO CONSIDER OTHER COLUMNS in ANY AND ALL
		default:
			System.out.println("Util.java:unknown expression "
					+ y.toStringTree());
			break;
		}
		return true;
	}

	public static fatworm.type.Field eval(Tree y) {
		switch (y.getType()) {
		case fatworm.parser.FatwormLexer.T__109:
		case fatworm.parser.FatwormLexer.T__108:
		case fatworm.parser.FatwormLexer.T__113:
		case fatworm.parser.FatwormLexer.T__105:
			return Value.calcOp(eval(y.getChild(0)), eval(y.getChild(1)), y.getType());
		case fatworm.parser.FatwormLexer.T__111:
			if (y.getChildCount() > 1)
				return Value.calcOp(eval(y.getChild(0)), eval(y.getChild(1)), y.getType());
			else{
				fatworm.type.Field v=eval(y.getChild(0));
				if(v instanceof INT)v=new INT(-((INT)v).v);
				else v=new FLOAT(-v.toDecimal().doubleValue());
				return v;
			}
		case FatwormLexer.NULL:
			return new NULL();
		case FatwormLexer.SELECT:
		case FatwormLexer.SELECT_DISTINCT:
			SelectPlan x = (SelectPlan) new Transfer().DFS(y);
			Scan yScan = x.toScan();
			try {
				yScan.getMeta();
				yScan.beforeFirst();
				if (yScan.next()) {
					Tuple tp = yScan.getTuple();
					return tp.getColumn(0).getField();
				}
				return new NULL();
			} catch (Throwable e) {
				Error.print(e);
				return new NULL();
			}
		default:
			return Env.getValue(y);
		}
	}

	public static Field getDefault(ColumnSchema col) {
		if (col.getDefault() != null) {
			return col.getDefault();
		} else if (col.getAutoValue() != null)
			return col.getAutoValueAfterInc();
		else {
			Field type = col.getType();
			if (type instanceof TIMESTAMP)
				return new TIMESTAMP(new java.sql.Timestamp(
						(new GregorianCalendar()).getTimeInMillis()).toString());
//			if (type instanceof DATE)
//				return new DATE(new java.sql.Timestamp(
//						(new GregorianCalendar()).getTimeInMillis()).toString());
			Error.print("unknown default value " + type.getClass().getName());
		}
		return new NULL();
	}

	public static Field[] getFieldArray(Tuple tuple) {
		Field[] ret=new Field[tuple.getSize()];
		for(int i=0;i<tuple.getSize();++i)
			ret[i]=tuple.getColumn(i).getField();
		return ret;
	}
}
