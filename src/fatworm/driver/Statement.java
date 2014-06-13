package fatworm.driver;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;





import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

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
import fatworm.scanner.Insert;
import fatworm.scanner.Scan;
import fatworm.util.Env;

import java.util.*;
public class Statement implements java.sql.Statement {
	private fatworm.driver.Connection connection;
	private ResultSet rs;
	public static StringBuilder myName;
	public static Map<String, String> aliasTable=new HashMap<String, String>();
	public Statement(fatworm.driver.Connection c) {
		connection = c;
	}
	public void setConnection(fatworm.driver.Connection c) {
		connection = c;
	}
	public fatworm.driver.Connection getConnection() {
		return connection;
	}
	public boolean execute(String sql) {
		aliasTable.clear();
		boolean isString=false;
		try{
			myName = new StringBuilder();
			for(int i=0;i<sql.length();++i){
				if(sql.charAt(i)=='"'||sql.charAt(i)=='\'')
					isString=!isString;
				if(!isString)
					myName.append(Character.toLowerCase(sql.charAt(i)));
				else 
					myName.append(sql.charAt(i));
			}
			this.rs=null;
			FatwormLexer lexer = new FatwormLexer(new ANTLRStringStream(myName.toString()));
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
				((SelectPlan)x).setTopLayer();
				Scan tt=((SelectPlan)x).toScan();
				this.rs=new ResultSet(tt);
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
			//System.gc();
		}catch(Throwable e){
			fatworm.util.Error.print(e);
			return false;
		}
		return true;
	}
	public ResultSet getResultSet() {
		return rs;
	}
	@Override
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		
		return false;
	}
	@Override
	public <T> T unwrap(Class<T> arg0) throws SQLException {
		
		return null;
	}
	@Override
	public void addBatch(String arg0) throws SQLException {
		
		
	}
	@Override
	public void cancel() throws SQLException {
		
		
	}
	@Override
	public void clearBatch() throws SQLException {
		
		
	}
	@Override
	public void clearWarnings() throws SQLException {
		
		
	}
	@Override
	public void close() throws SQLException {
		
		
	}
	@Override
	public boolean execute(String arg0, int arg1) throws SQLException {
		
		return false;
	}
	@Override
	public boolean execute(String arg0, int[] arg1) throws SQLException {
		
		return false;
	}
	@Override
	public boolean execute(String arg0, String[] arg1) throws SQLException {
		
		return false;
	}
	@Override
	public int[] executeBatch() throws SQLException {
		
		return null;
	}
	@Override
	public java.sql.ResultSet executeQuery(String arg0) throws SQLException {
		
		return null;
	}
	@Override
	public int executeUpdate(String arg0) throws SQLException {
		
		return 0;
	}
	@Override
	public int executeUpdate(String arg0, int arg1) throws SQLException {
		
		return 0;
	}
	@Override
	public int executeUpdate(String arg0, int[] arg1) throws SQLException {
		
		return 0;
	}
	@Override
	public int executeUpdate(String arg0, String[] arg1) throws SQLException {
		
		return 0;
	}
	@Override
	public int getFetchDirection() throws SQLException {
		
		return 0;
	}
	@Override
	public int getFetchSize() throws SQLException {
		
		return 0;
	}
	@Override
	public java.sql.ResultSet getGeneratedKeys() throws SQLException {
		
		return null;
	}
	@Override
	public int getMaxFieldSize() throws SQLException {
		
		return 0;
	}
	@Override
	public int getMaxRows() throws SQLException {
		
		return 0;
	}
	@Override
	public boolean getMoreResults() throws SQLException {
		
		return false;
	}
	@Override
	public boolean getMoreResults(int arg0) throws SQLException {
		
		return false;
	}
	@Override
	public int getQueryTimeout() throws SQLException {
		
		return 0;
	}
	@Override
	public int getResultSetConcurrency() throws SQLException {
		
		return 0;
	}
	@Override
	public int getResultSetHoldability() throws SQLException {
		
		return 0;
	}
	@Override
	public int getResultSetType() throws SQLException {
		
		return 0;
	}
	@Override
	public int getUpdateCount() throws SQLException {
		
		return 0;
	}
	@Override
	public SQLWarning getWarnings() throws SQLException {
		
		return null;
	}
	@Override
	public boolean isClosed() throws SQLException {
		
		return false;
	}
	@Override
	public boolean isPoolable() throws SQLException {
		
		return false;
	}
	@Override
	public void setCursorName(String arg0) throws SQLException {
		
		
	}
	@Override
	public void setEscapeProcessing(boolean arg0) throws SQLException {
		
		
	}
	@Override
	public void setFetchDirection(int arg0) throws SQLException {
		
		
	}
	@Override
	public void setFetchSize(int arg0) throws SQLException {
		
		
	}
	@Override
	public void setMaxFieldSize(int arg0) throws SQLException {
		
		
	}
	@Override
	public void setMaxRows(int arg0) throws SQLException {
		
		
	}
	@Override
	public void setPoolable(boolean arg0) throws SQLException {
		
		
	}
	@Override
	public void setQueryTimeout(int arg0) throws SQLException {
		
		
	}
	public void closeOnCompletion() throws SQLException {
		
		
	}
	public boolean isCloseOnCompletion() throws SQLException {
		
		return false;
	}
}
