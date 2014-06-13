package fatworm.scanner;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fatworm.bplus.Index;
import fatworm.driver.Connection;
import fatworm.files.Disk;
import fatworm.files.MemoryBuffer;
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
import fatworm.util.Error;

public class Table implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public MetaData meta;
	String dbname;
	public String tbname;
	public List<Field []> t;
	public Map<Integer, Index> sp=new HashMap<Integer, Index>(); 
	public static int CNT=0;
	public static final int FUCK=100;

	public Table(String db, String tb) {
		dbname = db;
		tbname = tb;
		t = new ArrayList<Field []>();
		clearIndex();
	}
	
	public void clearIndex(){
		if(!sp.isEmpty()){
		sp.clear();
		}
	}

	public Table(MetaData me, String db, String tb) {
		meta = me;
		dbname = db;
		tbname = tb;
		t = new ArrayList<Field []>();
		clearIndex();
	}

	public int size() {
		return t.size();
	}

	public void addField(Field[] x) {
		assert(x.length==meta.type.size());
		t.add(x);
	}

	public boolean delField(Field[] x) {
		return t.remove(x) ;
	}

	public Field[] getField(int x) {
		return t.get(x);
	}

	public static String recent = null;

	public void load() {
		recent = tbname;
		meta = MetaData.load(dbname + "_" + tbname);
		clearIndex();
		MemoryBuffer buffer = Disk.read(dbname + "_" + tbname + "_cnx");
		int cnt = buffer.getInt();
		for (int i = 0; i < cnt; ++i){
			Field arr[]=new Field[meta.type.size()];
			for(int j=0;j<meta.type.size();++j){
				boolean isNull = buffer.getBoolean();
				Field value = null;
				if (isNull)
					value = new NULL();
				else
					switch (meta.type.get(j).type.type) {
					case java.sql.Types.BOOLEAN:
						value = new BOOL();
						break;
					case java.sql.Types.CHAR:
						value = new CHAR();
						break;
					case java.sql.Types.DECIMAL:
						value = new DECIMAL();
						break;
					case java.sql.Types.FLOAT:
						value = new FLOAT();
						break;
					case java.sql.Types.INTEGER:
						value = new INT();
						break;
					case java.sql.Types.VARCHAR:
						value = new VARCHAR();
						break;
					case java.sql.Types.DATE:
						value = new DATE();
						break;
					case java.sql.Types.TIMESTAMP:
						value = new TIMESTAMP();
						break;
					default:
						value = new NULL();
					}
				value.load(buffer);
				arr[j]=value;
			}
			t.add(arr);
		}
	}

	public void save() {
		int cnt = MemoryBuffer.SIZE_OF_INT;
		for (int i = 0; i < t.size(); ++i)
			for(int j=0;j<meta.type.size();++j)
				if(t.get(i)[j] instanceof NULL)
					cnt+=1;
				else 
					cnt+=1+t.get(i)[j].getByteSize();
		MemoryBuffer buffer = new MemoryBuffer(cnt);
		buffer.putInt(t.size());
		for (int i = 0; i < t.size(); ++i)
			for(int j=0;j<meta.type.size();++j)
				if(t.get(i)[j] instanceof NULL)
					buffer.putBoolean(true);
				else{
					buffer.putBoolean(false);
					t.get(i)[j].save(buffer);
				}
		Disk.write(dbname+"_"+tbname+"_cnx",buffer);
//		if(!Disk.write(dbname + "_" + tbname + "_cnx", buffer))
//			Env.tables.add(this.tbname);
//		else
//			if(Env.tables.contains(this.tbname))
//				Env.tables.remove(this.tbname);
	}
	
	public void saveFrom(int pos) {
//		if(CNT>=FUCK){
//			Env.tables.add(this.tbname);
//			return;
//		}
//		else
//			++CNT;
		int cnt = 0;
		for (int i = pos; i < t.size(); ++i)
			for(int j=0;j<meta.type.size();++j)
				if(t.get(i)[j] instanceof NULL)
					cnt+=1;
				else 
					cnt+=1+t.get(i)[j].getByteSize();
		MemoryBuffer buffer = new MemoryBuffer(cnt);
		for (int i = pos; i < t.size(); ++i)
			for(int j=0;j<meta.type.size();++j)
				if(t.get(i)[j] instanceof NULL)
					buffer.putBoolean(true);
				else{
					buffer.putBoolean(false);
					t.get(i)[j].save(buffer);
				}
		Disk.appendFile(dbname+"_"+tbname+"_cnx", t.size(), buffer);
//		if(!Disk.appendFile(dbname + "_" + tbname + "_cnx", t.size(), buffer))
//			Env.tables.add(this.tbname);
	}

	public void drop() {
		try {
			MetaData.drop(dbname + "_" + tbname);
			File file = new File(Disk.dir+"/"+dbname + "_" + tbname + "_cnx");
			if (file.isFile() && file.exists())
				file.delete();
		} catch (Throwable e) {
			Error.print(e);
		}
	}
}
