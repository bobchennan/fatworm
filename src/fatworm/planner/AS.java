package fatworm.planner;

import org.antlr.runtime.tree.Tree;

public class AS extends Value implements Plan{
	public String alias;
	
	public AS(Tree _v, String _alias){
		super(_v);
		alias=_alias;
	}
	
	public AS(Value dfs, String string) {
		super(dfs.tt);
		op=dfs.op;
		l=dfs.l;
		r=dfs.r;
		alias=string;
	}

	@Override
	public String toString(){
		String ret="";
		switch(this.tt.getType()){
		case 81:
		case 82:
			ret+="subquery:\n"+new Transfer().DFS(tt).toString()+"\n";
			break;
		case 49:
			ret+="table: "+tt.toString()+"\n";
			break;
		default:
			ret+="value: "+tt.toString()+"\n";
			break;
		}
		ret+=Transfer.pre+"rename: "+alias+"\n";
		return ret;
	}
}
