package fatworm.planner;

import java.util.List;

public class InsertPlan implements Plan {
	public String table;
	public SelectPlan subquery=null;
	public List<Value> values=null;
	public List<ColumnName> col=null;
	
	public InsertPlan(String tbl, SelectPlan _subquery){
		table=tbl;
		subquery=_subquery;
	}
	
	public InsertPlan(String tbl, List<Value> _values){
		table=tbl;
		values=_values;
	}
	
	public InsertPlan(String tbl, List<ColumnName> _col, List<Value> _values){
		table=tbl;
		col=_col;
		values=_values;
	}
	
	@Override
	public String toString(){
		String ret=Transfer.pre+"InsertPlan:\n";
		Transfer.pre+="  ";
		if(subquery!=null)ret+=Transfer.pre+"QueryResult:\n"+subquery.toString();
		ret+=Transfer.pre+"Value Pairs:\n";
		Transfer.pre+="  ";
		for(int i=0;i<values.size();++i){
			if(col==null)ret+=col.get(i).toString();
			ret+=values.get(i).toString();
		}
		Transfer.pre=Transfer.pre.substring(0,Transfer.pre.length()-4);
		return ret;
	}
}
