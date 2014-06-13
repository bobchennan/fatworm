package fatworm.planner;

import org.antlr.runtime.tree.Tree;

import fatworm.util.Env;

public class DBS implements Plan {
	public static enum DBSc{CREATE,USE,DROP};
	Tree tt;
	DBSc op;
	
	public DBS(DBSc _op, Tree t){
		tt=t;
		op=_op;
	}
	
	@Override
	public String toString(){
//		switch(op){
//		case CREATE:
//			return Transfer.pre+"Create database "+table+"\n";
//		case USE:
//			return Transfer.pre+"Use database "+table+"\n";
//		case DROP:
//			return Transfer.pre+"Drop database "+table+"\n";
//		default:
//			return "ERROR in DBS!";
//		}
		return tt.toString();
	}
	
	public void execute(){
		switch(op){
		case CREATE:
			Env.addDB(tt.getChild(0).getText());
			break;
		case USE:
			Env.useDB(tt.getChild(0).getText());
			break;
		case DROP:
			Env.delDB(tt.getChild(0).getText());
			break;
		}
	}
}
