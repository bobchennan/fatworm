package fatworm.planner;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.tree.Tree;

import fatworm.parser.FatwormLexer;
import fatworm.type.DECIMAL;
import fatworm.type.FLOAT;
import fatworm.type.Field;
import fatworm.type.INT;
import fatworm.type.NULL;
import fatworm.util.Env;
import fatworm.util.Error;
import fatworm.util.Util;

public class Value implements Plan {
	public int op=-1;
	public Field v=null;
	Value l,r;
	public Tree tt=null;
	
	public Value(Tree t){
		tt=t;
	}
	
	public Value(Field _v){
		v=_v;
	}
	
	public Value(int _v){
		v=new INT(_v);
	}
	
	public Value(Value _l,Value _r,int o){
		l=_l;
		r=_r;
		op=o;
	}
	
	public List<Value> getAggregation(){
		List<Value> ret=new ArrayList<Value>();
		if(op==-1){
			if(tt!=null&&(tt.getType()==FatwormLexer.SUM||tt.getType()==FatwormLexer.COUNT||tt.getType()==FatwormLexer.AVG||tt.getType()==FatwormLexer.MIN||tt.getType()==FatwormLexer.MAX))
				ret.add(this);
		}
		else{
			ret.addAll(l.getAggregation());
			ret.addAll(r.getAggregation());
		}
		return ret;
	}
	
	public boolean calculate(){
		if(op==-1&&tt==null)return true;
		else if(tt!=null)return false;
		else{
			if(!l.calculate()||!r.calculate())return false;
			switch(op){
			case 109:
			case 111:
			case 108:
			case 113:
			case 105:
				v=getField();
				op=-1;
				break;
			default:
				return false;
			}
			return true;
		}
	}
	
	public static Field calcOp(Field ll, Field rr, int op){
		switch(op){
		case 109:return (ll instanceof INT && rr instanceof INT)?new INT(ll.toDecimal().intValue()+rr.toDecimal().intValue()):new FLOAT(ll.toDecimal().add(rr.toDecimal()).floatValue());
		case 111:return (ll instanceof INT && rr instanceof INT)?new INT(ll.toDecimal().intValue()-rr.toDecimal().intValue()):new FLOAT(ll.toDecimal().subtract(rr.toDecimal()).floatValue());
		case 108:return (ll instanceof INT && rr instanceof INT)?new INT(ll.toDecimal().intValue()*rr.toDecimal().intValue()):new FLOAT(ll.toDecimal().multiply(rr.toDecimal()).floatValue());
		case 113:return new FLOAT(ll.toDecimal().divide(rr.toDecimal()).floatValue());
		case 105:return new INT(ll.toDecimal().intValue()%rr.toDecimal().intValue());
		default:
			fatworm.util.Error.print("Value.java:unknown value for "+ll+" "+rr);
			return new NULL();
		}
	}
	
	public Field getField(){
		//TODO type besides int
		if(tt!=null)return Util.eval(tt);
		else if(op==-1&&tt==null)return v;
		else{
			Field ll=l.getField();
			Field rr=r.getField();
			return Value.calcOp(ll, rr, op);
		}
	}
	
	@Override
	public String toString(){
		if(calculate())return v.toString();
		else if(tt!=null)return tt.toStringTree();
		else{
			String bak=Transfer.pre;
			Transfer.pre+="  ";
			String ret="Op "+Integer.toString(op)+":\n";
			ret+=bak+"  "+l+"\n";
			ret+=bak+"  "+r;
			Transfer.pre=bak;
			return ret;
		}
	}
	
	public static int mergeType(int l, int r, int op){
		if(op==FatwormLexer.T__113)
			return java.sql.Types.FLOAT;
		if(l==r)return l;
		if(l==java.sql.Types.DECIMAL)return l;
		if(r==java.sql.Types.DECIMAL)return r;
		if(l==java.sql.Types.FLOAT)return l;
		if(r==java.sql.Types.FLOAT)return r;
		if(l==java.sql.Types.DATE)return l;
		if(r==java.sql.Types.DATE)return r;
		if(l==java.sql.Types.TIMESTAMP)return l;
		if(r==java.sql.Types.TIMESTAMP)return r;
		Error.print("unknown type merge "+l+" "+r);
		return l;
	}

	public int getType() {
		if(tt!=null)return Env.getType(tt);
		else if(op!=-1) return mergeType(l.getType(),r.getType(), op);
		else return getField().type;
	}
}
