package fatworm.scanner;

import java.util.List;

public interface Scan {
	public boolean next() throws Exception;
	public Object getObjectByIndex(int index) throws Exception;
	public void beforeFirst() throws Exception;
	public int getColumnCount() throws Exception;
	public Tuple getTuple() throws Exception;
	public TupleSchema getMeta();
}
