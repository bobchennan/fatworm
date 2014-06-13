package fatworm.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fatworm.files.Disk;
import fatworm.util.Env;
import fatworm.util.Error;

public class MetaData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public List<ColumnSchema> type=null;
	Map<String, Integer> h=null;
	
	public MetaData(){
		type=new ArrayList<ColumnSchema>();
		h=new HashMap<String, Integer>();
	}
	
	public int getId(String name){
		int pos=(name.lastIndexOf("."));
		if(pos!=-1)name=name.substring(pos+1);
		if(h.containsKey(name))return h.get(name);
		else return -1;
	}
	
	public int getType(int x){
		return Env.fromFieldtoType(type.get(x).getType());
	}
	
	public void addColumn(ColumnSchema x){
		type.add(x);
		h.put(x.name, type.size()-1);
	}
	
	public static MetaData load(String name){
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(Disk.dir+"/META_"+name,"r");
			FileInputStream fis = new FileInputStream(raf.getFD());
			ObjectInputStream oin = new ObjectInputStream(fis); 
			return (MetaData) oin.readObject();
		} catch (Throwable e) {
			Error.print(e);
		}
		return new MetaData();
	}
	
	public void save(String name){
		try {
		RandomAccessFile raf = new RandomAccessFile(Disk.dir+"/META_"+name,"rw");
		FileOutputStream fos = new FileOutputStream(raf.getFD());
		ObjectOutputStream oos = new ObjectOutputStream(fos); 
		oos.writeObject(this);
		oos.close();
	} catch (Throwable e) {
		Error.print(e);
	}
	}
	
	public static void drop(String name){
		try{
			File file = new File(Disk.dir+"/META_"+name);
			if(file.isFile() && file.exists())    
				file.delete();
		}catch (Throwable e) {
			Error.print(e);
		}
	}
}
