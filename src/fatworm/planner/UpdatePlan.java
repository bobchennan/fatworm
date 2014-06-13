package fatworm.planner;

import java.util.List;

import fatworm.scanner.Column;
import fatworm.scanner.Table;
import fatworm.scanner.Tuple;
import fatworm.scanner.TupleSchema;
import fatworm.type.Field;
import fatworm.type.INT;
import fatworm.util.Env;
import fatworm.util.Util;

public class UpdatePlan implements Plan {
	String table;
	List<ColumnName> col;
	List<Value> v;
	Expr where;
	
	public UpdatePlan(String tbl, List<ColumnName> _col, List<Value> _v, Expr _where){
		table=tbl;
		col=_col;
		v=_v;
		where=_where;
	}
	
	@Override
	public String toString(){
		String ret=Transfer.pre+"UpdatePlan:\n";
		return ret;
	}
	
	public void execute(){
		Table x=Env.getByName(table);
		x.clearIndex();
		TupleSchema xx=TupleSchema.fromMeta(x);
		Tuple nt=new Tuple();
		for(int j=0;j<xx.ty.size();++j)
			nt.addColumn(new Column(x.tbname, x.meta.type.get(0).name, null));
		for(int i=0;i<x.size();++i){
			Field[] nnt=x.getField(i);
			for(int j=0;j<xx.ty.size();++j)
				nt.getColumn(j).setValue(nnt[j]);
			nt.sch=xx;
			if(where==null||where.t==null||nt.match(where)){
				Env.addCommand(nt, xx);
				for(int j=0;j<col.size();++j){
					int idx=nt.sch.find(col.get(j).table,col.get(j).col);
					if(idx!=-1){
						nt.getColumn(idx).setValue(v.get(j).getField());
						nnt[idx]=nt.getColumn(idx).getField();
						if(xx.ty.get(idx)==java.sql.Types.INTEGER)
							nnt[idx]=new INT(nnt[idx].toDecimal().intValue());
					}
					else
						fatworm.util.Error.print("unknown "+col.get(j)+" in update");
				}
				Env.clearCommand();
			}
		}
		x.save();
	}
}
