package fatworm.driver;
import java.io.Serializable;
import java.util.*;

import fatworm.util.Env;

public class DatabaseMgr implements Serializable{
	private static final long serialVersionUID = 3L;
	public void setConnection(fatworm.driver.Connection c) {
		Env.init();
	}
	public DatabaseMgr(fatworm.driver.Connection c) {
	}
	public void close() {
		Env.save();
	}
	public void createDatabase(String s) {
		Env.addDB(s);
	}
	public void useDatabase(String s) {
		Env.useDB(s);
	}
	public void drop(String name) {
		Env.delDB(name);
	}
}
