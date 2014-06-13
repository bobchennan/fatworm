package fatworm.scanner;

import java.util.ArrayList;
import java.util.List;

import fatworm.parser.FatwormLexer;
import fatworm.planner.InsertPlan;
import fatworm.planner.Value;
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
import fatworm.util.Util;

public class Insert {
	InsertPlan tt;

	public Insert(InsertPlan t) {
		tt = t;
	}

	public void execute() {
		Table d = Env.getByName(tt.table);
		d.clearIndex();
		TupleSchema dd=TupleSchema.fromMeta(d);
		List<Field[]> added=new ArrayList<Field[]>();
		Field[] arr=new Field[d.meta.type.size()];
		boolean hasAuto = false;
		if (tt.subquery != null) {
			Scan x = tt.subquery.toScan();
			try {
				x.beforeFirst();
				x.getMeta();
				while (x.next()) {
					Tuple tt=x.getTuple();
					tt.sch=dd;
					for (int i = 0; i < tt.getSize(); ++i) {
						Field value=tt.getColumn(i).getField();
						if (d.meta.type.get(i).type instanceof DECIMAL)
							value = new DECIMAL(value.toDecimal());
						if (d.meta.type.get(i).type instanceof CHAR
								|| d.meta.type.get(i).type instanceof VARCHAR)
							value = Util.trim(value, d.meta.type.get(i).type);
						if (d.meta.type.get(i).type instanceof TIMESTAMP)
							value = new TIMESTAMP(((CHAR) value).v);
						if (d.meta.type.get(i).type instanceof DATE)
							value = new DATE(((CHAR) value).v);
						if (d.meta.type.get(i).type instanceof FLOAT && value instanceof INT)
							value = new FLOAT(((INT)value).v);
						arr[i]=value;
					}
					for (int i = 0; i < d.meta.type.size(); ++i){
						if(arr[i]!=null&&arr[i] instanceof INT&&d.meta.type.get(i).auto_increment&&d.meta.type.get(i).autoValue.compareTo(arr[i])==-1)
							d.meta.type.get(i).setAutoValue(arr[i]);
						if(arr[i]==null||(arr[i] instanceof NULL&&d.meta.type.get(i).auto_increment))
							arr[i]=Util.getDefault(d.meta.type.get(i));
					}
					added.add(arr);
					arr=new Field[d.meta.type.size()];
				}
			} catch (Throwable e) {
				Error.print(e);
			}
		} else {
			// TODO reorder it according to the order of schema
			// if(tt.col==null){
			for (int i = 0; i < tt.values.size(); ++i) {
				int idx = (tt.col == null) ? i : d.meta
						.getId(tt.col.get(i).col);
				Value v = tt.values.get(i);
				if(v.tt!=null&&v.tt.getType()==FatwormLexer.DEFAULT)
					continue;
				Field value;
				if (d.meta.type.get(i).type instanceof DECIMAL)
					value = v.tt == null ? new DECIMAL(v.v.toDecimal()) : new DECIMAL(
							v.tt.toString());
				else
					value = v.getField();
				if (d.meta.type.get(i).type instanceof CHAR
						|| d.meta.type.get(i).type instanceof VARCHAR)
					value = Util.trim(value, d.meta.type.get(i).type);
				if (d.meta.type.get(i).type instanceof TIMESTAMP)
					value = new TIMESTAMP(((CHAR) value).v);
				if (d.meta.type.get(i).type instanceof DATE)
					value = new DATE(((CHAR) value).v);
				if (d.meta.type.get(i).type instanceof FLOAT && value instanceof INT)
					value = new FLOAT(((INT)value).v);
				arr[idx]=value;
			}
			for (int i = 0; i < d.meta.type.size(); ++i){
				if(arr[i]!=null&&arr[i] instanceof INT&&d.meta.type.get(i).auto_increment&&d.meta.type.get(i).autoValue.compareTo(arr[i])==-1){
					d.meta.type.get(i).setAutoValue(arr[i]);
					hasAuto=true;
				}
				if(arr[i]==null||(arr[i] instanceof NULL&&d.meta.type.get(i).auto_increment))
					arr[i]=Util.getDefault(d.meta.type.get(i));
			}
			added.add(arr);
		}
		int cnt=d.size();
		for(int i=0;i<added.size();++i)
			d.addField(added.get(i));
		if(hasAuto)d.meta.save(d.dbname+"_"+d.tbname);
		d.saveFrom(cnt);
	}
}
