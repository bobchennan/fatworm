package fatworm.util;

public class Error {
	static boolean output=true;
	
	public static void print(Object e){
		if(output)System.err.println(e);
	}
	
	public static void print(Throwable e){
		if(output)e.printStackTrace();
	}
}
