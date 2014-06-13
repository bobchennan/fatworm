package fatworm.scanner;

import java.math.BigDecimal;
import java.sql.Types.*;

import com.sun.swing.internal.plaf.metal.resources.metal;

import fatworm.files.MemoryBuffer;
import fatworm.planner.Value;
import fatworm.type.BOOL;
import fatworm.type.CHAR;
import fatworm.type.DECIMAL;
import fatworm.type.FLOAT;
import fatworm.type.Field;
import fatworm.type.INT;
import fatworm.type.NULL;
import fatworm.type.VARCHAR;
import fatworm.util.Env;

@SuppressWarnings("unused")
public class Column {
	String tableName;
	String columnName;
//	String hisColumnName;
	Field value;
	public Column(String t, String c, Field v) {
		tableName = t;
		columnName = c;
//		hisColumnName = "";
		value = v;
	}
	public Column(String t, String c, String hist, Field v) {
		tableName = t;
		columnName = c;
//		hisColumnName = hist;
		value = v;
	}
//	public Column clone(){
//		return new Column(tableName,columnName,hisColumnName,value);
//	}
	public String getTableName() {
		return tableName;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setTableName(String s) {
		tableName = s;
	}
	public int getType() {
		return value.type;
	}
	public void setValue(Field v) {
		value = v;
	}
//	public boolean match(Value v) {
//		//TODO projection expression check
//		if(v.tt.getType()==fatworm.parser.FatwormLexer.AS)
//			v=new Value(v.tt.getChild(0));
//		if(v.toString().equals(tableName+"."+columnName)||v.toString().equals(tableName+"."+hisColumnName)||v.toString().equals(hisColumnName)||v.toString().equals(columnName))return true;
//		else return false;
//	}
//	public boolean match(String v){
//		if(v.equals(tableName+"."+columnName)||v.equals(tableName+"."+hisColumnName)||v.equals(hisColumnName)||v.equals(columnName))return true;
//		else return false;
//	}
	public Field getField() {
		return value;
	}
	@Override
	public boolean equals(Object o){
		if(o instanceof Column)
			return columnName.equalsIgnoreCase(((Column)o).columnName);
		else return toString().equalsIgnoreCase(o.toString());
	}
	@Override
	public String toString(){
		return value.toString();
	}
	public int getByteSize(){
		int ret=value.getByteSize();
		return ret;
	}
	@Override
	public int hashCode(){
		if(value instanceof BOOL)return new Boolean(((BOOL)value).v).hashCode();
		if(value instanceof CHAR)return new String(((CHAR)value).v).hashCode();
		if(value instanceof DECIMAL)return (((DECIMAL)value).v).hashCode();
		if(value instanceof FLOAT)return new Float(((FLOAT)value).v).hashCode();
		if(value instanceof INT)return new Integer(((INT)value).v).hashCode();
		if(value instanceof NULL)return 0;
		if(value instanceof VARCHAR)return new String(((VARCHAR)value).v).hashCode();
		return -1;
	}
}
