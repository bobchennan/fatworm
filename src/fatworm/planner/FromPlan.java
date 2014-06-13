package fatworm.planner;

import java.util.List;

public class FromPlan implements Plan {
    public List<Value> tables;
    
    public FromPlan(List<Value> t){
    	tables=t;
    }
    
    @Override
    public String toString(){
    	String ret=Transfer.pre+"FromPlan:\n";
    	Transfer.pre+="  ";
    	for(Value i:tables)
    		ret+=Transfer.pre+i.toString()+"\n";
    	Transfer.pre=Transfer.pre.substring(0,Transfer.pre.length()-2);
    	return ret;
    }
}
