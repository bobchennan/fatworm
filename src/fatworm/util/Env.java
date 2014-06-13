package fatworm.util;

import org.antlr.misc.MultiMap;
import org.antlr.runtime.tree.Tree;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fatworm.driver.Connection;
import fatworm.files.Disk;
import fatworm.parser.FatwormLexer;
import fatworm.planner.Value;
import fatworm.scanner.Column;
import fatworm.scanner.ColumnSchema;
import fatworm.scanner.MetaData;
import fatworm.scanner.Table;
import fatworm.scanner.Tuple;
import fatworm.scanner.TupleSchema;
import fatworm.type.*;

public class Env {
	static String db = null;
	static List<Tuple> vTuple=new ArrayList<Tuple>();
	static List<TupleSchema> schList=new ArrayList<TupleSchema>();
	static MultiMap<String, String> h = new MultiMap<String, String>();
	public static final Map<String, Table> hh=new HashMap<String,Table>();
	public final static boolean storedInMemory=true;

	// static Map<String, Table> h;
	// static List<Table> hh=null;
	public static void addCommand(Tuple x, TupleSchema sch) {
		vTuple.add(x);
		schList.add(sch);
	}
	public static void clearCommand(){
		vTuple.remove(vTuple.size()-1);
		schList.remove(schList.size()-1);
	}
	
	public static Column findColumn(String s){
		for(int i=vTuple.size()-1;i>=0;--i){
			Column retColumn=vTuple.get(i).getColumn(s);
			if(retColumn!=null)return retColumn;
//			int idx=schList.get(i).find(s);
//			if(idx!=-1)return vTuple.get(i).getColumn(idx);
		}
		Error.print("unknown column "+s);
		return null;
	}
	
	public static int findType(String s){
		for(int i=schList.size()-1;i>=0;--i){
			int idx=schList.get(i).find(s);
			if(idx!=-1)return schList.get(i).ty.get(idx);
		}
		Error.print("unknown type in Env.findType "+s);
		return java.sql.Types.INTEGER;
	}
	
	public static int getType(Tree t) {
		if (t.getType()==fatworm.parser.FatwormLexer.T__111&&t.getChildCount()==1)
			return new Value(t.getChild(0)).getType();
		switch (t.getType()) {
		case fatworm.parser.FatwormLexer.T__112:
			return findType(t.getChild(0).getText()+"."+t.getChild(1).getText());
		case fatworm.parser.FatwormLexer.STRING_LITERAL:
			return java.sql.Types.CHAR;
		case fatworm.parser.FatwormLexer.INTEGER_LITERAL:
			return java.sql.Types.INTEGER;
		case fatworm.parser.FatwormLexer.FLOAT_LITERAL:
			return java.sql.Types.FLOAT;
		case fatworm.parser.FatwormLexer.TRUE:
		case fatworm.parser.FatwormLexer.FALSE:
			return java.sql.Types.BOOLEAN;
		case fatworm.parser.FatwormLexer.NULL:
			return java.sql.Types.NULL;
		case fatworm.parser.FatwormLexer.T__109:
		case fatworm.parser.FatwormLexer.T__111:
		case fatworm.parser.FatwormLexer.T__108:
		case fatworm.parser.FatwormLexer.T__113:
		case fatworm.parser.FatwormLexer.T__105:
			return Value.mergeType(getType(t.getChild(0)), getType(t.getChild(1)),t.getType());
		}
		return findType(t.toStringTree());
		// System.err.print("unknown "+t.toStringTree());
		// return new NULL();
	}

	public static Field getValue(Tree t) {
		switch(t.getType()){
		case fatworm.parser.FatwormLexer.T__112:// .
			return findColumn(
					t.getChild(0).getText() + "." + t.getChild(1).getText())
					.getField();
		case fatworm.parser.FatwormLexer.STRING_LITERAL:
			return new CHAR(t.getText());
		case fatworm.parser.FatwormLexer.INTEGER_LITERAL:
			return new INT(Integer.parseInt(t.getText()));
		case fatworm.parser.FatwormLexer.FLOAT_LITERAL:
			return new FLOAT(Float.parseFloat(t.getText()));
		case fatworm.parser.FatwormLexer.TRUE:
			return new BOOL(Boolean.parseBoolean(t.getText()));
		case fatworm.parser.FatwormLexer.FALSE:
			return new BOOL(Boolean.parseBoolean(t.getText()));
		case fatworm.parser.FatwormLexer.NULL:
			return new NULL();
		case fatworm.parser.FatwormLexer.DEFAULT:
			return new CHAR(t.getText());
//		if (t.getType() == fatworm.parser.FatwormLexer.ID)
//			return findColumn(t.toStringTree()).getField();
		//Error.print(t.toStringTree());
		default:
			Column tmp=findColumn(t.toStringTree());
			return tmp!=null?tmp.getField():new NULL();
		}
		// System.err.print("unknown "+t.toStringTree());
		// return new NULL();
	}

	public static String getName(Tree t) {
		if(t==null)return "";
		if (t.getType() == fatworm.parser.FatwormLexer.T__112)// .
			return findColumn(
					t.getChild(0).getText() + "." + t.getChild(1).getText())
					.getColumnName();
		if (t.getType() == fatworm.parser.FatwormLexer.ID)
			return findColumn(t.toStringTree()).getColumnName();
		// TODO DEFAULT
		Column ret = findColumn(t.toStringTree());
		return ret != null ? ret.getColumnName() : "";
		// System.err.print("unknown "+t.toStringTree());
		// return new NULL();
	}

	public static void useDB(String name) {
		if(storedInMemory&&db!=name){
			hh.clear();
		}
		db = name;
		// h=new HashMap<String, Table>();
		// hh=h.get(name);
		// System.out.println("OK, use database "+name);
	}

	public static Table getByName(String name) {
		// System.out.println(name);
		// for(Table i:hh)
		// if(i.tbname.equals(name))
		// return i;
		// System.err.println("Cannot find table "+name);
		// if(h.get(name)!=null)return h.get(name);
		if(storedInMemory)
			if(hh.containsKey(name))
				return hh.get(name);
		Table ret = new Table(db, name);
		if (h.containsKey(db)&&h.get(db).contains(name)){
			ret.load();
			if(storedInMemory)
				hh.put(name, ret);
		}
		else
			fatworm.util.Error.print("Env: no such table " + name);
		// h.put(name, ret);
		return ret;
	}

	public static void delDB(String name) {
		if (!h.containsKey(name))
			return;
		String bak = db;
		useDB(name);
		if(storedInMemory)hh.clear();
		try {
			for (String i : h.get(name)) {
				File file = new File(Disk.dir + "/" + db + "_" + i + "_cnx");
				if (file.isFile() && file.exists())
					file.delete();
				file = new File(Disk.dir + "/META_" + db + "_" + i);
				if (file.isFile() && file.exists())
					file.delete();
			}
		} catch (Throwable e) {
			Error.print(e);
		}
		h.get(name).clear();
		useDB(bak);
		save();
	}

	public static void save() {
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(Disk.dir + "/" + "FATWORM_DB", "rw");
			FileOutputStream fos = new FileOutputStream(raf.getFD());
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(h);
			oos.close();
		} catch (Throwable e) {
			Error.print(e);
		}
	}

	public static void addDB(String name) {
		if (!h.containsKey(name))
			h.put(name, new ArrayList<String>());
		save();
	}

	public static void init() {
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(Disk.dir + "/" + "FATWORM_DB", "r");
			FileInputStream fos = new FileInputStream(raf.getFD());
			ObjectInputStream oos = new ObjectInputStream(fos);
			h = (MultiMap<String, String>) oos.readObject();
			oos.close();
		} catch (Throwable e) {
			Error.print(e);
		}
	}

	public static void delTb(String name) {
		Table d = new Table(db, name);
		if(storedInMemory)h.remove(name);
		d.drop();
		h.get(db).remove(name);
	}

	public static void addTable(Tree t) {
		if (t.getType() != fatworm.parser.FatwormLexer.CREATE_TABLE)
			fatworm.util.Error.print("unknown addTable operation");
		MetaData x = new MetaData();
		for (int ch = 1; ch < t.getChildCount(); ++ch) {
			ColumnSchema tmp = new ColumnSchema();
			tmp.setName(t.getChild(ch).getChild(0).getText());
			if (t.getChild(ch).getChildCount() > 1) {
				switch (t.getChild(ch).getChild(1).getType()) {
				case fatworm.parser.FatwormLexer.INT:
					tmp.setType(new INT());
					break;
				case fatworm.parser.FatwormLexer.FLOAT:
					tmp.setType(new FLOAT());
					break;
				case fatworm.parser.FatwormLexer.CHAR:
					tmp.setType(new CHAR(Integer.parseInt(t.getChild(ch)
							.getChild(1).getChild(0).toString())));
					break;
				case fatworm.parser.FatwormLexer.DATETIME:
					tmp.setType(new DATE());
					break;
				case fatworm.parser.FatwormLexer.BOOLEAN:
					tmp.setType(new BOOL(false));
					break;
				case fatworm.parser.FatwormLexer.DECIMAL:
					tmp.setType(new DECIMAL());
					break;
				case fatworm.parser.FatwormLexer.TIMESTAMP:
					tmp.setType(new TIMESTAMP());
					break;
				case fatworm.parser.FatwormLexer.VARCHAR:
					tmp.setType(new VARCHAR(Integer.parseInt(t.getChild(ch)
							.getChild(1).getChild(0).toString())));
					break;
				}
				for (int j = 2; j < t.getChild(ch).getChildCount(); ++j) {
					if (t.getChild(ch).getChild(j).getType() == fatworm.parser.FatwormLexer.NULL)
						tmp.setNotNull(true);
					if (t.getChild(ch).getChild(j).getType() == fatworm.parser.FatwormLexer.AUTO_INCREMENT)
						tmp.setAutoIncrement(true);
					if (t.getChild(ch).getChild(j).getType() == fatworm.parser.FatwormLexer.DEFAULT)
						tmp.setDefault(getValue(t.getChild(ch).getChild(j)
								.getChild(0)));
				}
				x.addColumn(tmp);
			} else if (t.getChild(ch).getChild(0).getType() == FatwormLexer.PRIMARY_KEY) {// TODO
																							// search
																							// primary
																							// key
			}
		}
		x.save(db + "_" + t.getChild(0).getText());
		Table retTable=new Table(x, db, t.getChild(0).getText());
		retTable.save();
		h.get(db).add(t.getChild(0).getText());
		if(storedInMemory)hh.put(t.getChild(0).getText(), retTable);
		save();
	}

	public static int fromFieldtoType(Field x) {
		if (x instanceof BOOL)
			return java.sql.Types.BOOLEAN;
		if (x instanceof CHAR)
			return java.sql.Types.CHAR;
		if (x instanceof DECIMAL)
			return java.sql.Types.DECIMAL;
		if (x instanceof FLOAT)
			return java.sql.Types.FLOAT;
		if (x instanceof INT)
			return java.sql.Types.INTEGER;
		if (x instanceof NULL)
			return java.sql.Types.NULL;
		if (x instanceof VARCHAR)
			return java.sql.Types.VARCHAR;
		if (x instanceof DATE || x instanceof TIMESTAMP)
			return java.sql.Types.TIMESTAMP;
		fatworm.util.Error.print("unknown type " + x);
		return -100000;
	}
}
