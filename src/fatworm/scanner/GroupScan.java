package fatworm.scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.antlr.misc.MultiMap;
import org.antlr.runtime.tree.Tree;

import fatworm.parser.FatwormLexer;
import fatworm.planner.AS;
import fatworm.planner.ColumnName;
import fatworm.planner.Value;
import fatworm.type.FLOAT;
import fatworm.type.Field;
import fatworm.type.INT;
import fatworm.util.Env;
import fatworm.util.Error;
import fatworm.util.Util;

public class GroupScan implements Scan {

	public Scan lowScan;
	public ColumnName col;
	Map<Field, Tuple> h;
	public List<Value> hList;
	Iterator iter;
	Tuple last = null;
	TupleSchema sch = null;

	public GroupScan(Scan _low, ColumnName _col, List<Value> p) {
		lowScan = _low;
		col = _col;
		hList = p;
	}

	@Override
	public boolean next() throws Exception {
		last = null;
		return iter.hasNext();
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
		h = new LinkedHashMap<Field, Tuple>();
		MultiMap<Integer, Integer> dic = new MultiMap<Integer, Integer>();
		MultiMap<Integer, Integer> fdic = new MultiMap<Integer, Integer>();
//		List<Double> avgs = new ArrayList<Double>();
//		List<Integer> avgc = new ArrayList<Integer>();
		Map<Field, Map<Integer, Integer>> avgc=new HashMap<Field, Map<Integer,Integer>>();
		Map<Field, Map<Integer, Double>> avgs=new HashMap<Field, Map<Integer,Double>>();
		TupleSchema lowSchema=lowScan.getMeta();
		while (lowScan.next()) {
			Tuple t = lowScan.getTuple();
			t.sch=lowScan.getMeta();
			Column tmpColumn=t.getColumn(col.toString());
			Field v=null;
			if(tmpColumn!=null)
				v=tmpColumn.getField();
			else{
				for(int i=0;i<hList.size();++i)
					if(hList.get(i) instanceof AS){
						if(((AS)hList.get(i)).alias.equals(col.toString())){
							v=t.getColumn(hList.get(i).tt.getText()).getField();
							break;
						}
					}
			}
			Tuple old = h.get(v);
			if (old == null) {
				avgc.put(v, new HashMap<Integer, Integer>());
				avgs.put(v, new HashMap<Integer, Double>());
				dic.clear();
				fdic.clear();
				old = new Tuple(sch);
				for (int i = 0; i < hList.size(); ++i) {
					Tree tt = hList.get(i).tt;
					String colname = "";
					if (hList.get(i) instanceof AS)
						colname = ((AS) hList.get(i)).alias;
					else if (tt != null && tt.getType() == FatwormLexer.AS) {
						colname = tt.getChild(1).getText();
						tt = tt.getChild(0);
					}
					if (hList.get(i).tt == null)
						old.addColumn(new Column("", colname, hList.get(i)
								.getField()));// TODO type besides int
					else {
						if ((tt.getChildCount() == 0 ||this.col.col.equals(tt.getText()))
								|| (tt.getChildCount() == 2 && this.col
										.toString().equals(
												tt.getChild(0).getText()
														+ "."
														+ tt.getChild(1)
																.getText()))) {
							if (colname != "")
								old.addColumn(new Column(
										tt.getChildCount() > 0 ? tt.getChild(0)
												.getText() : "", colname, (tt
												.getChildCount() > 1 && tt
												.getChild(1) == null) ? tt
												.getChild(1).getText():"", null));
							else
								old.addColumn(new Column(
										tt.getChildCount() > 0 ? tt.getChild(0)
												.getText() : "", (tt
												.getChildCount() < 2 || tt
												.getChild(1) == null) ? tt
												.toString() : tt.getChild(1)
												.getText(), null));
							put(dic, lowSchema.find(tt), i);
							put(fdic, lowSchema.find(tt), -999999);
							continue;
						}
						String col = (tt.getChild(0).getType() == FatwormLexer.T__112) ? tt
								.getChild(0).getChild(0).toString()
								+ "." + tt.getChild(0).getChild(1).toString()
								: tt.getChild(0).toString();
						int ccol=lowSchema.find(col);
						switch (tt.getType()) {
						case FatwormLexer.MAX:
							old.addColumn(new Column("", colname, tt
									.toStringTree(), new FLOAT(Float.MIN_VALUE)));
							put(dic, ccol, i);
							put(fdic, ccol, tt.getType());
							break;
						case FatwormLexer.MIN:
							old.addColumn(new Column("", colname, tt
									.toStringTree(), new FLOAT(Float.MAX_VALUE)));
							put(dic, ccol, i);
							put(fdic, ccol, tt.getType());
							break;
						case FatwormLexer.COUNT:
						case FatwormLexer.SUM:
							old.addColumn(new Column(
									"",
									colname,
									tt.toStringTree(),
									tt.getType() == FatwormLexer.SUM ? new FLOAT(
											0) : new INT(0)));
							put(dic, ccol, i);
							put(fdic, ccol, tt.getType());
							break;
						case FatwormLexer.AVG:
							old.addColumn(new Column("", colname, tt
									.toStringTree(), new FLOAT(0)));
							avgs.get(v).put(i, 0.0);
							avgc.get(v).put(i, 0);
							put(dic, ccol, i);
							put(fdic, ccol, i);
//							avgs.add((double) 0);
//							avgc.add(0);
							break;
						default:
							Error.print("doesn't know "
									+ tt.toStringTree() + " in GroupScan");
						}
					}
				}
				h.put(v, old);
			}
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
					Field vField = old.getColumn(idx.get(r)).value;
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
//						Double sum = (avgs.get(-way.get(r)) + j.value
//								.toDecimal().doubleValue());
//						Integer cnt = avgc.get(-way.get(r)) + 1;
//						avgs.set(-way.get(r), sum);
//						avgc.set(-way.get(r), cnt);
						Integer cnt=avgc.get(v).get(idx.get(r))+1;
						Double sum=avgs.get(v).get(idx.get(r))+j.value.toDecimal().doubleValue();
						vField = new FLOAT(sum * 1.0 / cnt);
						avgc.get(v).put(idx.get(r),cnt);
						avgs.get(v).put(idx.get(r),sum);
					}
					Column ch = old.getColumn(idx.get(r));
					ch.value = vField;
				}
			}
		}
		iter = h.entrySet().iterator();
	}

	@Override
	public int getColumnCount() throws Exception {
		return hList.size() + 1;
	}

	@Override
	public Tuple getTuple() throws Exception {
		if (last == null) {
			Entry<Field, Tuple> tmp = (Map.Entry<Field, Tuple>) iter.next();
			last = tmp.getValue();
		}
		return last;
	}

	@Override
	public TupleSchema getMeta() {
		if (sch != null)
			return sch;
		sch = new TupleSchema();
		TupleSchema tmp = lowScan.getMeta();
		for (int i = 0; i < hList.size(); ++i) {
			Tree tt = hList.get(i).tt;
			String colname = "", table = "";
			if (hList.get(i) instanceof AS)
				colname = ((AS) hList.get(i)).alias;
			else if (tt != null && tt.getType() == FatwormLexer.AS) {
				colname = tt.getChild(1).getText();
				tt = tt.getChild(0);
			}
			if (colname == ""&&tt!=null)
				if (!tt.toString().equals("."))
					colname = tt.toStringTree();
				else {
					table = tt.getChild(0).toString();
					colname = tt.getChild(1).toString();
				}
			if (hList.get(i).tt == null){
				Env.addCommand(null, tmp);
				sch.add(table, colname, hList.get(i).getType());
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
					int idx=tmp.find(table, colname);
					if(idx!=-1){
						Env.addCommand(null, tmp);
						int ty=hList.get(i).getType();
						sch.add(table, colname, ty);
						sch.getFrom(tmp, idx, ty);
						Env.clearCommand();
					}
					else{
						//Error.print("doesn't find "+table+" "+colname+" in "+sch+" in GroupScan");
						Env.addCommand(null, tmp);
						sch.add(table, tt.getText(), hList.get(i).getType());
						if(hList.get(i) instanceof AS){
							sch.add(table, ((AS)hList.get(i)).alias);
						}
						Env.clearCommand();
					}
				}
			}
		}
		return sch;
	}
}
