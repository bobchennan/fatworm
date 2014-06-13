package fatworm.scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.misc.MultiMap;
import org.antlr.runtime.tree.Tree;

import fatworm.parser.FatwormLexer;
import fatworm.planner.AS;
import fatworm.planner.Value;
import fatworm.type.FLOAT;
import fatworm.type.Field;
import fatworm.type.INT;
import fatworm.util.Env;
import fatworm.util.Error;

public class ProjectScan implements Scan {

	public Scan lowScan;
	public List<Value> sList;
	Tuple nextTuple;
	boolean nogroup = false;
	TupleSchema sch = null;
	public boolean groupProject=false;
	
	public void set(){
		groupProject=true;
	}

	public ProjectScan(Scan _low, List<Value> select) {
		lowScan = _low;
		sList = select;
	}
	
	public ProjectScan(Scan _low, List<Value> select, boolean SB) {
		lowScan = _low;
		sList = select;
		groupProject=SB;
	}

	int fist;
	@Override
	public boolean next() throws Exception {
		++fist;
		if (!nogroup) {
			nextTuple = null;
			return lowScan.next();
		} else
			return fist==1;
			
	}

	@Override
	public Tuple getTuple() throws Exception {
		if (nogroup) {
			nextTuple.sch=getMeta();
			return nextTuple;
		} else if (nextTuple == null)
			nextTuple = lowScan.getTuple().match(sList);
		nextTuple.sch=getMeta();
		return nextTuple;
	}

	@Override
	public Object getObjectByIndex(int index) throws Exception {
		return getTuple().getColumn(index);
	}

	private void put(MultiMap<Integer, Integer> dic, Integer col, int i) {
		if (!dic.containsKey(col))
			dic.put(col, new ArrayList<Integer>());
		List<Integer> d = dic.get(col);
		d.add(i);
	}

	@Override
	public void beforeFirst() throws Exception {
		lowScan.beforeFirst();
		if (nogroup) {
			MultiMap<Integer, Integer> dic = new MultiMap<Integer, Integer>();
			MultiMap<Integer, Integer> fdic = new MultiMap<Integer, Integer>();
			List<Double> avgs = new ArrayList<Double>();
			List<Integer> avgc = new ArrayList<Integer>();
			nextTuple = new Tuple();
			TupleSchema lowSchema=lowScan.getMeta();
			for (int i = 0; i < sList.size(); ++i) {
				Tree tt = sList.get(i).tt;
				String colname = "";
				if (sList.get(i) instanceof AS)
					colname = ((AS) sList.get(i)).alias;
				else if (tt != null && tt.getType() == FatwormLexer.AS) {
					colname = tt.getChild(1).getText();
					tt = tt.getChild(0);
				}
				if (sList.get(i).tt == null)
					nextTuple.addColumn(new Column("", colname, sList.get(i)
							.getField()));// TODO type besides int
				else {
					String col = (tt.getChild(0).getType() == FatwormLexer.T__112) ? tt
							.getChild(0).getChild(0).toString()
							+ "." + tt.getChild(0).getChild(1).toString()
							: tt.getChild(0).toString();
					int ccol=lowSchema.find(col);
					switch (tt.getType()) {
					case FatwormLexer.MAX:
						nextTuple.addColumn(new Column("", colname, tt
								.toStringTree(), new FLOAT(Integer.MIN_VALUE)));
						put(dic, ccol, i);
						put(fdic, ccol, tt.getType());
						break;
					case FatwormLexer.MIN:
						nextTuple.addColumn(new Column("", colname, tt
								.toStringTree(), new FLOAT(Integer.MAX_VALUE)));
						put(dic, ccol, i);
						put(fdic, ccol, tt.getType());
						break;
					case FatwormLexer.COUNT:
					case FatwormLexer.SUM:
						nextTuple.addColumn(new Column("", colname, tt
								.toStringTree(),
								tt.getType() == FatwormLexer.SUM ? new FLOAT(0)
										: new INT(0)));
						put(dic, ccol, i);
						put(fdic, ccol, tt.getType());
						break;
					case FatwormLexer.AVG:
						nextTuple.addColumn(new Column("", colname, tt
								.toStringTree(), new FLOAT(0)));
						put(dic, ccol, i);
						put(fdic, ccol, -avgs.size());
						avgs.add((double) 0);
						avgc.add(0);
						break;
					default:
						System.err.print("doesn't know " + tt.toStringTree()
								+ " in ProjectScan");
					}
				}
			}
			while (lowScan.next()) {
				Tuple t = lowScan.getTuple();
				t.sch=lowScan.getMeta();
				for (int i = 0; i < t.getSize(); ++i) {
					Column j = t.getColumn(i);
					List<Integer> idx = null, way = null;
					if (dic.get(i) != null) {
						idx = dic.get(i);
						way = fdic.get(i);
					}
					if (idx == null)
						continue;
					for (int r = 0; r < idx.size(); ++r) {
						Field vField = nextTuple.getColumn(idx.get(r)).value;
						switch (way.get(r)) {
						case FatwormLexer.MAX:
							vField = (j.value.compareTo(vField) == -1) ? vField
									: j.value;
							break;
						case FatwormLexer.MIN:
							vField = (j.value.compareTo(vField) == 1) ? vField
									: j.value;
							break;
						case FatwormLexer.SUM:
							vField = new FLOAT(vField.toDecimal().doubleValue()
									+ j.value.toDecimal().doubleValue());
							break;
						case FatwormLexer.COUNT:
							vField = new INT(((INT) vField).v + 1);
							break;
						case -999999:
							vField = j.value;
							break;
						default:// AVG
							Double sum = (avgs.get(-way.get(r)) + j.value
									.toDecimal().doubleValue());
							Integer cnt = avgc.get(-way.get(r)) + 1;
							avgs.set(-way.get(r), sum);
							avgc.set(-way.get(r), cnt);
							vField = new FLOAT(sum * 1.0 / cnt);
						}
						Column ch = nextTuple.getColumn(idx.get(r));
						ch.value = vField;
					}
				}
			}
		}
	}

	@Override
	public int getColumnCount() throws Exception {
		return sList.size();
	}

	@Override
	public TupleSchema getMeta() {
		if(!groupProject)
			for (Value i : sList) {
				if (i.tt != null) {
					Tree t = i.tt;
					while (t.getType() == FatwormLexer.AS)
						t = t.getChild(0);
					if (t.toString().equalsIgnoreCase("max"))
						nogroup = true;
					if (t.toString().equalsIgnoreCase("count"))
						nogroup = true;
					if (t.toString().equalsIgnoreCase("sum"))
						nogroup = true;
					if (t.toString().equalsIgnoreCase("avg"))
						nogroup = true;
					if (t.toString().equalsIgnoreCase("min"))
						nogroup = true;
					if (nogroup)
						break;
				}
			}
		if (sch != null)
			return sch;
		sch = new TupleSchema();
		TupleSchema tmp = lowScan.getMeta();
		if (nogroup)
			for (int i = 0; i < sList.size(); ++i) {
				Tree tt = sList.get(i).tt;
				String colname = "", table = "";
				if (sList.get(i) instanceof AS)
					colname = ((AS) sList.get(i)).alias;
				else if (tt != null && tt.getType() == FatwormLexer.AS) {
					colname = tt.getChild(1).getText();
					tt = tt.getChild(0);
				}
				if (colname == "" && tt != null)
					if (!tt.toString().equals("."))
						colname = tt.toStringTree();
					else {
						table = tt.getChild(0).toString();
						colname = tt.getChild(1).toString();
					}
				if (sList.get(i).tt == null){
					Env.addCommand(null, tmp);
					sch.add(table, colname, sList.get(i).getType());
					Env.clearCommand();
				}
				else {
					switch (tt.getType()) {
					case FatwormLexer.MAX:
					case FatwormLexer.MIN:
						Env.addCommand(null, tmp);
						sch.add(table, colname, Env.getType(tt.getChild(0)));
						Env.clearCommand();
						break;
					case FatwormLexer.SUM:
					case FatwormLexer.AVG:
						sch.add(table, colname, java.sql.Types.FLOAT);
						break;
					case FatwormLexer.COUNT:
						sch.add(table, colname, java.sql.Types.INTEGER);
						break;
					default:
						Error.print("doesn't know " + tt.toStringTree()
								+ " in ProjectScan Schema");
						sch.add(table, colname, tmp.find(table+"."+colname));
					}
				}
			}
		else {
			for (int i = 0; i < sList.size(); ++i) {
				Tree tt = sList.get(i).tt;
				String colname = "", table = "";
				if (sList.get(i) instanceof AS)
					colname = ((AS) sList.get(i)).alias;
				else if (tt != null && tt.getType() == FatwormLexer.AS) {
					colname = tt.getChild(1).getText();
					tt = tt.getChild(0);
				}
				if (colname == "" && tt != null)
					if (!tt.toString().equals("."))
						colname = tt.toStringTree();
					else {
						table = tt.getChild(0).toString();
						colname = tt.getChild(1).toString();
					}
				if (sList.get(i).tt == null){
					Env.addCommand(null, tmp);
					sch.add(table, colname, sList.get(i).getType());
					Env.clearCommand();
				}
				else {
					int idx = tmp.find(table, colname);
					if(idx==-1)idx=tmp.find(table, tt.toStringTree());
					if (idx == -1) {
						Error.print("doesn't find " + table + " " + colname
								+ " in " + sch+" in ProjectScan");
						sch.add(table, colname, sList.get(i).getType());
					} else
						sch.add(table, colname, tmp.getType().get(idx));
				}
			}
		}
		return sch;
	}
}
