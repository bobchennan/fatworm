package fatworm.planner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.antlr.runtime.tree.Tree;

import fatworm.scanner.AddColScan;
import fatworm.scanner.AsScan;
import fatworm.scanner.ConditionScan;
import fatworm.scanner.ConstantScan;
import fatworm.scanner.DistinctScan;
import fatworm.scanner.GroupScan;
import fatworm.scanner.ProductScan;
import fatworm.scanner.ProjectScan;
import fatworm.scanner.Scan;
import fatworm.scanner.SortScan;
import fatworm.scanner.TableScan;
import fatworm.scanner.Tuple;
import fatworm.util.Env;
import fatworm.driver.Statement;
import fatworm.opt.Optimization;
import fatworm.parser.FatwormLexer;

public class SelectPlan implements Plan {
	boolean distinct = false;
	List<Value> select_as = null;
	List<ColumnName> orderbylist = null;
	List<Boolean> order = null;
	Expr having = null;
	FromPlan from = null;
	public Expr where = null;
	ColumnName groupby = null;
	int appSize=0;
	boolean topLayer=false;
	boolean star=false;

	public SelectPlan(boolean dis, List<Value> x, List<ColumnName> a,
			List<Boolean> f, Expr b, FromPlan c, Expr d, ColumnName e, boolean star) {
		distinct = dis;
		select_as = x;
		orderbylist = a;
		order = f;
		having = b;
		from = c;
		where = d;
		groupby = e;
		this.star=star;
	}
	
	public void setTopLayer(){
		topLayer=true;
	}
	
	public int approximateSize(){
		if(appSize!=0)return appSize;
		int ret=1;
		if(from==null)return ret;
		for(Value i:from.tables){
			ret*=approximateSize(i);
		}
		appSize=ret/(where!=null?(1+where.split().size()):1);
		return ret;
	}
	
	public int approximateSize(Value i){
		Tree j;
		if(i instanceof AS)
			j=((AS)i).tt;
		else
			j=i.tt;
		int ret;
		if(j.getType()==FatwormLexer.SELECT||j.getType()==FatwormLexer.SELECT_DISTINCT){
			ret=((SelectPlan) new Transfer().DFS(j)).approximateSize();
		}
		//else ret=Env.getByName(j.toString()).size()/(1<<((Statement.myName.toString().split(" "+j.toString()+".", -1).length-1)+1));
		else ret=where!=null?(where.t.toStringTree().split(j.toString(), -1).length-1):0+1;
		return ret;
	}

	public Scan toScan() {
		Scan base = null, tmp;
		if (from != null){
			if(select_as!=null&&select_as.size()>0)
				Collections.sort(from.tables, new Comparator<Value>(){
				public int compare(Value a, Value b){
					int s1=approximateSize(a);
					//System.out.println(a.toString()+s1);
					int s2=approximateSize(b);
					//System.out.println(b.toString()+s2);
					return s1==s2?0:(s1>s2?-1:1);
				}
			});
			//Collections.reverse(from.tables);
			for (Value i : from.tables) {
				if (i instanceof AS)
					if (i.tt.getType() == FatwormLexer.SELECT
							|| i.tt.getType() == FatwormLexer.SELECT_DISTINCT)
						tmp = new AsScan(
								((SelectPlan) (new Transfer().DFS(i.tt)))
										.toScan(),
								((AS) i).alias);
					else
						tmp = new AsScan(new TableScan(i.tt.toString()),
								((AS) i).alias);
				else if (i.tt.getType() == FatwormLexer.SELECT
						|| i.tt.getType() == FatwormLexer.SELECT_DISTINCT)
					tmp = ((SelectPlan) new Transfer().DFS(i.tt)).toScan();
				else
					tmp = new TableScan(i.tt.toString());
				if (base == null)
					base = tmp;
				else
					base = new ProductScan(base,tmp);
			}
		}
		else
			base = new ConstantScan();
		if (where != null)
			base = new ConditionScan(base, new Expr(where.t));
		if (having != null) {
			boolean dt = false;
			List<Value> bak = new ArrayList<Value>();
			bak.addAll(select_as);
			for (int i = 0; i < having.t.getChildCount(); ++i) {
				int ty = having.t.getChild(i).getType();
				if (ty == FatwormLexer.SUM || ty == FatwormLexer.AVG
						|| ty == FatwormLexer.COUNT || ty == FatwormLexer.MAX
						|| ty == FatwormLexer.MIN) {
					select_as.add(new Value(having.t.getChild(i)));
					dt = true;
				}
			}
			base = new GroupScan(base, groupby, select_as);
			if (topLayer&&orderbylist != null && orderbylist.size() != 0)
				base = new SortScan(base, orderbylist, order);
			base = new ConditionScan(base, new Expr(having.t));
			if (dt){
				base = new ProjectScan(base, bak);
				((ProjectScan)base).set();
			}
		} else if (select_as != null && select_as.size() != 0 && !star)
			if (groupby == null){
				List<Value> preList=new ArrayList<Value>(); 
				for(int i=0;i<select_as.size();++i){
					preList.addAll(select_as.get(i).getAggregation());
				}
				if (topLayer&&orderbylist != null && orderbylist.size() != 0)
					base = new SortScan(base, orderbylist, order);
				if(preList.size()!=0&&preList.size()!=select_as.size())
					base=new ProjectScan(base, preList);
				base = new ProjectScan(base, select_as);
			}
			else{
				base = new GroupScan(base, groupby, select_as);
				if (topLayer&&orderbylist != null && orderbylist.size() != 0)
					base = new SortScan(base, orderbylist, order);
			}
		else{
			if(star&&(select_as!=null&&select_as.size()>0))
				base=new AddColScan(base, select_as);
			if (topLayer&&orderbylist != null && orderbylist.size() != 0)
				base = new SortScan(base, orderbylist, order);
		}
		if (distinct)
			base = new DistinctScan(base);
		return Optimization.optimize(base);
	}

	@Override
	public String toString() {
		String ret = "";
		String bak = Transfer.pre;
		if (orderbylist.size() != 0) {
			ret += Transfer.pre + "Sort by " + orderbylist.toString() + "\n";
			Transfer.pre += "  ";
		}
		if (having != null) {
			ret += Transfer.pre + "Having condition: " + having.toString()
					+ "\n";
			Transfer.pre += "  ";
		}
		ret += Transfer.pre
				+ (distinct ? "SelectPlan(distinct):" : "SelectPlan:") + "\n";
		Transfer.pre += "  ";
		if (select_as == null || select_as.size() == 0)
			ret += Transfer.pre + "ALL\n";
		else
			for (Value i : select_as)
				ret += Transfer.pre + i.toString() + "\n";
		if (from != null)
			ret += from.toString();
		if (where != null)
			ret += Transfer.pre + "Having condition: " + where.toString()
					+ "\n";
		if (groupby != null)
			ret += Transfer.pre + "Group by " + groupby.toString() + "\n";
		Transfer.pre = bak;
		return ret;
	}
}
