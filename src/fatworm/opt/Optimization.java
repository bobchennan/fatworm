package fatworm.opt;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.tree.Tree;

import fatworm.planner.Expr;
import fatworm.planner.Value;
import fatworm.scanner.*;
import fatworm.util.Util;

public class Optimization {
	
	public static Scan DFS(Scan s, List<Expr> limit){
		if(s instanceof TableScan){
			if(limit.size()>0){
				Tree isRange=null;
				for(int i=0;i<limit.size();++i)
					if(limit.get(i).isZeroPattern()){
						if(s.getMeta().canExecute(limit.get(i).t.getChild(0))){
							isRange=limit.get(i).t.getChild(0);
							break;
						}
					}
				if(isRange==null)return new ConditionScan(s, limit);
				List<Integer> op=new ArrayList<Integer>();
				List<Value> v=new ArrayList<Value>();
				List<Expr> limit2=new ArrayList<Expr>();
				for(int i=0;i<limit.size();++i){
					if(limit.get(i).isZeroPattern()){
						if(limit.get(i).t.getChild(0).toStringTree().equals(isRange.toStringTree())){
							op.add(limit.get(i).t.getType());
							v.add(new Value(limit.get(i).t.getChild(1)));
						}else if(limit.get(i).t.getChild(1).toStringTree().equals(isRange.toStringTree())){
							op.add(limit.get(i).t.getType());
							v.add(new Value(limit.get(i).t.getChild(0)));
						}else{
							limit2.add(limit.get(i));
						}
					}else{
						limit2.add(limit.get(i));
					}
				}
				if(limit2.size()>0)return new ConditionScan(new RangeScan(((TableScan)s).tbname, isRange, op, v), limit2);
				else return new RangeScan(((TableScan)s).tbname, isRange, op, v);
			}
			else return s;
		}
		else if(s instanceof ConditionScan){
			List<Expr> limit2=new ArrayList<Expr>();
			limit2.addAll(limit);
			limit2.addAll(((ConditionScan)s).cond);
			return DFS(((ConditionScan)s).lowScan, limit2);
		}
		else if(s instanceof AsScan){
			List<Expr> now=new ArrayList<Expr>();
			List<Expr> give=new ArrayList<Expr>();
			for(int i=0;i<limit.size();++i)
				if(Util.contains(limit.get(i).t,((AsScan)s).alias))
					now.add(limit.get(i));
				else
					give.add(limit.get(i));
			if(now.size()>0)return new ConditionScan(new AsScan(DFS(((AsScan)s).lowScan, give),((AsScan)s).alias), now);
			else return new AsScan(DFS(((AsScan)s).lowScan, give),((AsScan)s).alias);
		}
		else if(s instanceof ConstantScan){
			if(limit.size()>0)return new ConditionScan(s, limit);
			else return s;
		}
		else if(s instanceof DistinctScan)
			return new DistinctScan(DFS(((DistinctScan)s).lowScan, limit));
		else if(s instanceof GroupScan){
			List<Expr> now=new ArrayList<Expr>();
			List<Expr> give=new ArrayList<Expr>();
			for(int i=0;i<limit.size();++i)
				if(Util.contains(limit.get(i).t, "max")||Util.contains(limit.get(i).t, "min")||Util.contains(limit.get(i).t, "count")||Util.contains(limit.get(i).t, "sum")||Util.contains(limit.get(i).t, "avg"))
					now.add(limit.get(i));
				else{
					if(((GroupScan)s).lowScan.getMeta().canExecute(limit.get(i).t))
						give.add(limit.get(i));
					else
						now.add(limit.get(i));
				}
			if(now.size()>0)return new ConditionScan(new GroupScan(DFS(((GroupScan)s).lowScan, give),((GroupScan)s).col,((GroupScan)s).hList), now);
			else return new GroupScan(DFS(((GroupScan)s).lowScan, give),((GroupScan)s).col,((GroupScan)s).hList);
		}
		else if(s instanceof ProductScan){
			TupleSchema sa=((ProductScan)s).sa.getMeta();
			TupleSchema sb=((ProductScan)s).sb.getMeta();
			List<Expr> l1=new ArrayList<Expr>();
			List<Expr> l2=new ArrayList<Expr>();
			List<Expr> now=new ArrayList<Expr>();
			int onePatern=-1;
			for(int i=0;i<limit.size();++i){
				if(sa.canExecute(limit.get(i).t))
					l1.add(limit.get(i));
				else if(sb.canExecute(limit.get(i).t))
					l2.add(limit.get(i));
				else{
					now.add(limit.get(i));
					if(onePatern==-1&&s.getMeta().canExecute(limit.get(i).t))
						if(limit.get(i).isOnePattern())
							onePatern=now.size()-1;
				}
			}
			Scan sa1=DFS(((ProductScan)s).sa,l1);
			Scan sb1=DFS(((ProductScan)s).sb,l2);
			Scan retScan;
			if(onePatern!=-1)
				retScan=new OnePatternScan(sa1, sb1, now, onePatern);
			else{
				retScan=new ProductScan(sa1,sb1);
				if(now.size()>0)
					retScan=new ConditionScan(retScan,now);
			}
			return retScan;
		}
		else if(s instanceof ProjectScan){
			List<Expr> now=new ArrayList<Expr>();
			List<Expr> give=new ArrayList<Expr>();
			TupleSchema schema=((ProjectScan)s).lowScan.getMeta();
			for(int i=0;i<limit.size();++i)
				if (schema.canExecute(limit.get(i).t))
					give.add(limit.get(i));
				else
					now.add(limit.get(i));
			if(now.size()>0)return new ConditionScan(new ProjectScan(DFS(((ProjectScan)s).lowScan, give),((ProjectScan)s).sList,((ProjectScan)s).groupProject), now);
			else return new ProjectScan(DFS(((ProjectScan)s).lowScan, give),((ProjectScan)s).sList,((ProjectScan)s).groupProject);
		}
		else if(s instanceof SortScan)return new SortScan(DFS(((SortScan)s).lowScan,limit), ((SortScan)s).col, ((SortScan)s).ord);
		else if(s instanceof AddColScan)
			return new AddColScan(DFS((((AddColScan)s).lowScan), limit), ((AddColScan)s).toadd);
		else if(limit.size()>0)return new ConditionScan(s, limit);
		else return s;
//		else if(s instanceof ProductScan){
//			
//		}
	}
	
	public static Scan optimize(Scan s){
		//return s;
		return DFS(s, new ArrayList<Expr>());
	}
}
