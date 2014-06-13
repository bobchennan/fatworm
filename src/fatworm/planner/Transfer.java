package fatworm.planner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

import fatworm.driver.Statement;
import fatworm.planner.DBS.DBSc;
import fatworm.type.INT;

public class Transfer {
//	String DFS(Tree t, String pre){
//		String ret="";
//		switch(t.getType()){
//		case FatwormLexer.SELECT:
//		case FatwormLexer.SELECT_DISTINCT:
//			ret+=(pre+"SELECT QUERY"+(t.getType()==FatwormLexer.SELECT_DISTINCT?" distinct":"")+'\n');
//			for(int i=0;i<t.getChildCount();++i){
//				ret+=DFS(t.getChild(i),pre+"  ");
//			}
//			break;
//		//case FatwormLexer.WHERE:
//			//ret+=(pre+"WHERE"+"\n");
//			//ret+=DFS(t.getChild(0),pre+"  ");
//			//break;
//		//case FatwormLexer.CREATE_DATABASE:
//			//break;
//		default:
//			ret+=(pre+t.toString()+'\n');
//			for(int i=0;i<t.getChildCount();++i)
//				ret+=DFS(t.getChild(i),pre+"  ");
//		}
//		return ret;
//	}
	public static String pre="";
	public Plan DFS(Tree t){
		switch(t.getType()){
		case 82://select distinct
		case 81://select
			boolean dis=(t.getType()==82);
			List<Value> exprs=new ArrayList<Value>();
			List<ColumnName> orderby=new ArrayList<ColumnName>();
			List<Boolean> order=new ArrayList<Boolean>();
			Expr having=null;
			FromPlan from=null;
			Expr where=null;
			ColumnName groupby=null;
			boolean star=false;
			for (int i=0;i<t.getChildCount();++i){
				Tree tt=t.getChild(i);
				switch(tt.getType()){
				case 43:from=(FromPlan)DFS(tt);break;
				case 100:where=(Expr)DFS(tt);break;
				case 47:having=(Expr)DFS(tt);break;
				case 45:groupby=(ColumnName)DFS(tt);break;
				case 74:
					Tree ttt;
					for(int j=0;j<tt.getChildCount();++j){
						if(tt.getChild(j).getType()==30)
							order.add(true);
						else
							order.add(false);
						if(tt.getChild(j).getType()==30||tt.getChild(j).getType()==9)
							ttt=tt.getChild(j).getChild(0);
						else
							ttt=tt.getChild(j);
						if(ttt.getType()==49)
							orderby.add(new ColumnName(ttt.toString()));
						else
							orderby.add(new ColumnName(ttt.getChild(0).toString(),ttt.getChild(1).toString()));
					}
					break;
				case 108:
					if(tt.getChildCount()==0)star=true;
					else exprs.add((Value)DFS(tt));
					break;
				default:
					Plan tmpPlan=DFS(tt);
					if(tmpPlan instanceof Value)exprs.add((Value)tmpPlan);
					else if(tt.getType()==fatworm.parser.FatwormLexer.SELECT&&tt.getChildCount()==1&&tt.getChild(0).getChildCount()==0)
						exprs.add(new Value(tt.getChild(0)));
					else exprs.add(new Value(tt));
				}
			}
			if(exprs.size()==0)
				exprs=null;//TODO fuck *,4
			return new SelectPlan(dis,exprs,orderby,order,having,from,where,groupby,star);
		case 43://from
			exprs=new ArrayList<Value>();
			for(int i=0;i<t.getChildCount();++i)
				exprs.add((Value)DFS(t.getChild(i)));
			return new FromPlan(exprs);
		case 100://where
			return new Expr(t.getChild(0));
		case 47://having
			return new Expr(t.getChild(0));
		case 45://group
			Tree tt=t.getChild(0);
			if(tt.getType()==112)//.
				return new ColumnName(tt.getChild(0).toString(),tt.getChild(1).toString());
			else
				return new ColumnName(t.getChild(0).toString());
		case 55://insert values
			List<Value> vv=new ArrayList<Value>();
			tt=t.getChild(t.getChildCount()-1);
			for(int i=0;i<tt.getChildCount();++i)
				vv.add((Value)DFS(tt.getChild(i)));
			return new InsertPlan(t.getChild(0).toString(), vv);
		case 53://insert columns
			List<ColumnName> v=new ArrayList<ColumnName>();
			for(int i=1;i<t.getChildCount();++i)
				v.add(new ColumnName(t.getChild(i).toString()));
			vv=new ArrayList<Value>();
			tt=t.getChild(t.getChildCount()-1);
			for(int i=0;i<tt.getChildCount();++i)
				vv.add((Value)DFS(tt.getChild(i)));
			return new InsertPlan(t.getChild(0).toString(), v, vv);
		case 54://insert subquery
			return new InsertPlan(t.getChild(0).toString(), (SelectPlan)DFS(t.getChild(1)));
		case 92://update
			List<ColumnName> col=new ArrayList<ColumnName>();
			List<Value> va=new ArrayList<Value>();
			where=null;
			for(int i=1;i<t.getChildCount();++i){
				if(t.getChild(i).getType()==100)
					where=(Expr)DFS(t.getChild(i));
				else{
					col.add(new ColumnName(t.getChild(i).getChild(0).toString()));
					va.add(new Value(t.getChild(i).getChild(1)));
				}
			}
			return new UpdatePlan(t.getChild(0).toString(), col, va, where);
		case 8://as
			Plan retPlan=DFS(t.getChild(0));
			Statement.aliasTable.put(t.getChild(1).getText(),t.getChild(0).toStringTree());
			if(retPlan instanceof Value)
				return new AS((Value)retPlan,t.getChild(1).toString());
			else {
				return new AS(t.getChild(0),t.getChild(1).toString());
			}
		case 57://int const
			try{
				return new Value(Integer.parseInt(t.toString()));
			}catch(Throwable e){
				return new Value(t);
			}
		case 109:// add
		case 111:// sub
		case 105:// mod
		case 113:// div
		case 108:// mul
			Value son=(Value)DFS(t.getChild(0));
			if(t.getChildCount()>1)return new Value(son,(Value)DFS(t.getChild(1)),t.getType());
			else if(t.getType()==111&&t.getChildCount()==1&&(son).calculate())
				return new Value(-((INT)son.getField()).v);
			else return new Value(t);
		case fatworm.parser.FatwormLexer.CREATE_DATABASE:
			return new DBS(DBSc.CREATE,t);
		case fatworm.parser.FatwormLexer.USE_DATABASE:
			return new DBS(DBSc.USE,t);
		case fatworm.parser.FatwormLexer.DROP_DATABASE:
			return new DBS(DBSc.DROP,t);
		case fatworm.parser.FatwormLexer.DELETE:
			return new DeletePlan(t.getChild(0).getText(), new Expr(t.getChild(1)));
		default:
			return new Value(t);
		}
	}
	
	public void load(CommonTree t){
		Plan ret=DFS((Tree)t);
		System.out.println(ret);
	}
}
