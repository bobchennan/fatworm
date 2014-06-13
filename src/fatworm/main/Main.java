package fatworm.main;

import java.math.BigDecimal;
import java.util.Scanner;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;

import fatworm.parser.FatwormLexer;
import fatworm.parser.FatwormParser;
import fatworm.planner.DBS;
import fatworm.planner.DeletePlan;
import fatworm.planner.InsertPlan;
import fatworm.planner.Plan;
import fatworm.planner.SelectPlan;
import fatworm.planner.Transfer;
import fatworm.planner.UpdatePlan;
import fatworm.planner.Value;
import fatworm.scanner.*;
import fatworm.util.Env;
import fatworm.driver.*;
import fatworm.opt.*;

public class Main {
	public static void main(String[] args){
		Scanner in = new Scanner(System.in);
		System.out.println("Welcome to cnx's database");
		//System.out.println(new BigDecimal(2002).setScale(10).divide(new BigDecimal(2).setScale(10)).floatValue());
		StringBuilder tmp=new StringBuilder();
		Env.init();
		while (in.hasNextLine()) {
			try {
				tmp.append(" "+in.nextLine());
				if(tmp.toString().endsWith(";")){tmp.deleteCharAt(tmp.length()-1);
				if(tmp.toString().equalsIgnoreCase("quit")||tmp.toString().equalsIgnoreCase("exit")||tmp.toString().equalsIgnoreCase("\\q"))break;
				FatwormLexer lexer = new FatwormLexer(new ANTLRStringStream(tmp.toString()));
				CommonTokenStream tokens = new CommonTokenStream(lexer);
				FatwormParser parser = new FatwormParser(tokens);
				FatwormParser.statement_return rs = parser.statement();
				CommonTree t = (CommonTree) rs.getTree();
				//System.out.println(t.toStringTree());
				Transfer tran=new Transfer();
				Plan x=tran.DFS(t);
				if(x instanceof InsertPlan)
					new Insert((InsertPlan)x).execute();
				if(x instanceof DBS)
					((DBS)x).execute();
				if(x instanceof SelectPlan){
					Scan tt=((SelectPlan)x).toScan();
					tt.beforeFirst();
					while(tt.next()){
						System.out.println(tt.getTuple());
					}
				}
				if(x instanceof Value){
					if(((Value)x).tt.getType()==fatworm.parser.FatwormLexer.CREATE_TABLE)
						Env.addTable(((Value)x).tt);
					if(((Value)x).tt.getType()==fatworm.parser.FatwormLexer.DROP_TABLE)
						Env.delTb(((Value)x).tt.getChild(0).getText());
				}
				if(x instanceof DeletePlan)
					((DeletePlan)x).execute();
				if(x instanceof UpdatePlan)
					((UpdatePlan)x).execute();
				tmp.delete(0, tmp.length());
				}else{
					if(tmp.charAt(tmp.length()-1)=='\n')tmp.deleteCharAt(tmp.length()-1);
				}
			}catch(Throwable e){
				fatworm.util.Error.print(e);
			}
		}
	}
}
