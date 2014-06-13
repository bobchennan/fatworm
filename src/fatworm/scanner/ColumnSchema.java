package fatworm.scanner;

import java.io.Serializable;

import fatworm.type.Field;
import fatworm.type.INT;

public class ColumnSchema implements Serializable {
		private static final long serialVersionUID = 8L;
		public String name;
		Field type;
		boolean notNull;
		boolean auto_increment;
		Field defa=null;
		Field autoValue=null;
		public ColumnSchema() {
			type = null;
			notNull = false;
			auto_increment = false;
			defa = null;
		}
		public void setType(Field t) {
			type = t;
		}
		public Field getType() {
			return type;
		}
		public void setNotNull(boolean n) {
			notNull = n;
		}
		public boolean getNotNull() {
			return notNull;
		}
		
		public void setAutoIncrement(boolean a) {
			auto_increment = a;
			if (a)
				//TODO auto_increment for different types
				autoValue = new INT(0);
		}
		public Field getAutoValueAfterInc() {
			autoValue = new INT(((INT)autoValue).v+1);
			return autoValue;
		}
		public boolean getAutoIncrement() {
			return auto_increment;
		}
		public Field getAutoValue() {
			return autoValue;
		}
		public void setAutoValue(Field tt) {
			autoValue = tt;
		}
		public void setDefault(Field d) {
			defa = d;
		}
		public Field getDefault() {
			return defa;
		}
		public void setName(String n) {
			name = n;
		}
		public String getName() {
			return name;
		}
	}

