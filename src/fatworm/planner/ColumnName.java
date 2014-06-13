package fatworm.planner;

public class ColumnName implements Plan{
    public String table="";
    public String col="";
    
    public ColumnName(String a, String b){
    	table=a;
    	col=b;
    }
    
    public ColumnName(String b){
    	col=b;
    }
    
    @Override
    public String toString(){
    	if(table!="")return new String(table+"."+col);
    	else return new String(col);
    }
    
    @Override
    public boolean equals(Object x){
    	if(x instanceof ColumnName&&((ColumnName)x).table.equals(table)&&((ColumnName)x).col.equals(col))
    		return true;
    	else return false;
    }
    
    @Override
	public int hashCode() {
		return (table+"."+col).hashCode();
	}
}
