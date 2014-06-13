package fatworm.bplus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.ietf.jgss.Oid;

import fatworm.type.Field;
import fatworm.type.NULL;
import fatworm.util.Error;

public class Index {
	public class record implements java.lang.Comparable{
		public Field v;
		public int p;
		public record(Field v, int p){
			this.v=v;
			this.p=p;
		}
		public int compareTo(Object e){
			return v.compareTo(((record)e).v);
		}
	}
	public List<record> h;
	public List<Integer> p; 
	public Field lowerBound=null;
	public Field upperBound=null;
	boolean b1,b2;
	
	public Index(){
		h=new ArrayList<record>();
		p=new ArrayList<Integer>();
	}
	
	public Index clone(){
		Index ret=new Index();
		ret.h=h;
		ret.p=p;
		ret.lowerBound=lowerBound;
		ret.upperBound=upperBound;
		ret.b1=ret.b2=true;
		return ret;
	}
	
	public void insert(Field x, int y){
		if(!(x instanceof NULL))
			h.add(new record(x,y));
	}
	
	public void finish(){
		Collections.sort(h);
		lowerBound=h.get(0).v;
		upperBound=h.get(h.size()-1).v;
		for(record i:h)
			p.add(i.p);
	}
	
	public void findSmallerThan(Field x, boolean include){
		int v=upperBound.compareTo(x);
		if(v==1){
			upperBound=x;
			b2=include;
		}
		if(v==0){
			b1&=include;
		}
	}
	
	public void findLargerThan(Field x, boolean include){
		int v=lowerBound.compareTo(x);
		if(v==-1){
			lowerBound=x;
			b1=include;
		}
		if(v==0){
			b1&=include;
		}
	}
	
//	public NavigableMap<Field, Integer> findBetween(Field x, Field y, boolean include1, boolean include2){
//		return h.subMap(x, include1, y, include2);
//	}
	
	public List<Integer> iterator(){
		if(lowerBound.compareTo(upperBound)==1)return new ArrayList<Integer>();
		if(lowerBound.compareTo(upperBound)==0&&((!b1)||(!b2)))return new ArrayList<Integer>();
		int l=0,r=h.size()-1,mid;
		Field vField;
		if(b1){//>=
			while(l!=r){
				mid=((l+r)>>1);
				vField=h.get(mid).v;
				if(vField.compareTo(lowerBound)==-1)l=mid+1;
				else r=mid;
			}
		}else{//>
			while(l!=r){
				mid=((l+r)>>1);
				vField=h.get(mid).v;
				if(vField.compareTo(lowerBound)!=1)l=mid+1;
				else r=mid;
			}
		}
		
		int ll=l,rr=h.size()-1;
		if(b2){//<=
			while(ll!=rr){
				mid=((ll+rr+1)>>1);
				vField=h.get(mid).v;
				if(vField.compareTo(upperBound)==1)rr=mid-1;
				else ll=mid;
			}
		}else{
			while(ll!=rr){
				mid=((ll+rr+1)>>1);
				vField=h.get(mid).v;
				if(vField.compareTo(upperBound)!=-1)rr=mid-1;
				else ll=mid;
			}
		}
		return p.subList(l, ll+1);
	}
}
