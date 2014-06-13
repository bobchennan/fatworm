package fatworm.planner;

public class IndexPlan implements Plan {
	boolean unique=false, drop=false;
	String index_name;
	String tbl,col;
	
	public IndexPlan(boolean _u, String in, String _tbl, String _col){
		unique=_u;
		index_name=in;
		tbl=_tbl;
		col=_col;
	}
	
	public IndexPlan(String in, String _tbl){
		drop=true;
		index_name=in;
		tbl=_tbl;
	}
	
	@Override
	public String toString(){
		String ret=Transfer.pre+"IndexPlan:\n";
		Transfer.pre+="  ";
		ret+=Transfer.pre+"Name: "+index_name+"\n";
		ret+=Transfer.pre+"On table: "+tbl+"\n";
		if(col!=null)ret+=Transfer.pre+"On column: "+col+"\n";
		Transfer.pre=Transfer.pre.substring(0,Transfer.pre.length()-2);
		return ret;
	}
}
