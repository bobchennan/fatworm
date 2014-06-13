package fatworm.planner;

import java.util.Iterator;

import com.sun.corba.se.impl.javax.rmi.CORBA.Util;

import fatworm.scanner.Column;
import fatworm.scanner.Table;
import fatworm.scanner.Tuple;
import fatworm.scanner.TupleSchema;
import fatworm.type.Field;
import fatworm.util.Env;

public class DeletePlan implements Plan {
	String table;
	Expr where;
	
	public DeletePlan(String tbl, Expr _where){
		table=tbl;
		where=_where;
	}
	
	public void execute(){
		Table x=Env.getByName(table);
		x.clearIndex();
		TupleSchema xx=TupleSchema.fromMeta(x);
		for(Iterator<Field[]> it=x.t.iterator();it.hasNext();){
			Field[] nnt=it.next();
			Tuple nt=new Tuple();
			for(int i=0;i<xx.ty.size();++i)
				nt.addColumn(new Column(x.tbname, x.meta.type.get(0).name, nnt[i]));
			nt.sch=xx;
			if(where==null||where.t==null||nt.match(new Expr(where.t.getChild(0))))
				it.remove();
		}
		x.save();
	}
	
	@Override
	public String toString(){
		String ret="";
		ret+=Transfer.pre+"Delete Plan:"+"\n";
		Transfer.pre+="  ";
		ret+=where.toString();
		Transfer.pre=Transfer.pre.substring(0,Transfer.pre.length()-2);
		return ret;
	}
}
