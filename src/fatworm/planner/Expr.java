package fatworm.planner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

import fatworm.parser.FatwormLexer;
import fatworm.scanner.TupleSchema;

public class Expr implements Plan {
	public Tree t;
	public Expr(Tree _t){
		t=_t;
	}
	
	public List<Expr> split(){
		List<Expr> ret=new ArrayList<Expr>();
		if(t.getType()==FatwormLexer.OR){
			List<Expr> ll=(new Expr(t.getChild(0))).split();
			List<Expr> rr=(new Expr(t.getChild(1))).split();
			for(int i=0;i<ll.size();++i)
				for(int j=0;j<rr.size();++j){
					CommonTree tt=new CommonTree((CommonTree) t);
					tt.addChild(ll.get(i).t);
					tt.addChild(rr.get(j).t);
					ret.add(new Expr(tt));
				}
			return ret;
		}
		else if(t.getType()!=FatwormLexer.AND){
			ret.add(this);
			return ret; 
		}
		else{
			for(int i=0;i<t.getChildCount();++i)
				ret.addAll(new Expr(t.getChild(i)).split());
			return ret;
		}
	}
	
	@Override
	public String toString(){
		return "Expr: "+t.toStringTree();
	}
	
	public boolean isOnePattern() {
		if(t.getType()==FatwormLexer.T__117&&t.getChildCount()==2)
			if((t.getChild(0).getText().equals(".")&&t.getChild(0).getChildCount()==2)||t.getChild(0).getChildCount()==0)
				if((t.getChild(1).getText().equals(".")&&t.getChild(1).getChildCount()==2)||t.getChild(1).getChildCount()==0)
					return true;
		return false;
	}

	public boolean isZeroPattern() {
		if((t.getType()==FatwormLexer.T__114||t.getType()==FatwormLexer.T__115||t.getType()==FatwormLexer.T__118||t.getType()==FatwormLexer.T__119)&&t.getChildCount()==2)
			if((t.getChild(0).getText().equals(".")&&t.getChild(0).getChildCount()==2)||(t.getChild(0).getChildCount()==0&&t.getChild(0).getType()==FatwormLexer.ID)||(t.getChild(0).getText().equals("-")&&t.getChild(0).getChildCount()==1))
				if((t.getChild(1).getText().equals(".")&&t.getChild(1).getChildCount()==2)||(t.getChild(1).getChildCount()==0&&t.getChild(0).getType()==FatwormLexer.ID)||(t.getChild(1).getText().equals("-")&&t.getChild(1).getChildCount()==1))
					if(new TupleSchema().canExecute(t.getChild(0))||new TupleSchema().canExecute(t.getChild(1)))
						return true;
		return false;
	}
}
