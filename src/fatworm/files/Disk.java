package fatworm.files;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Disk {

	public static String dir = ".";
	public static String lastfilename = "";
	public static RandomAccessFile raf = null;
	public static FileChannel channel = null;

	static void openFile(String filename) {
		try {
			if (!filename.equals(lastfilename)) {
				if (raf != null) {
					raf.close();
					channel.close();
				}
				raf = new RandomAccessFile(dir + "/" + filename, "rw");
				channel = raf.getChannel();
				lastfilename = filename;
			} else
				channel.position(0);
		} catch (Throwable e) {
			fatworm.util.Error.print(e);
		}
	}

	public static void write(String filename, MemoryBuffer buffer) {
		try {
			openFile(filename);
			channel.write(ByteBuffer.wrap(buffer.getBuffer()));
		} catch (Throwable e) {
			fatworm.util.Error.print(e);
		}
	}

	public static void appendFile(String filename, int add, MemoryBuffer buffer) {
		try {
			openFile(filename);
			ByteBuffer sizeBuffer = ByteBuffer
					.wrap(new byte[] { (byte) add, (byte) (add >> 8),
							(byte) (add >> 16), (byte) (add >> 24) });
			while (sizeBuffer.hasRemaining())
				channel.write(sizeBuffer);

			channel.position(channel.size());
			ByteBuffer addBuffer = ByteBuffer.wrap(buffer.getBuffer());
			while (addBuffer.hasRemaining())
				channel.write(addBuffer);
			// channel.close();
			// raf.close();
		} catch (Throwable e) {
			fatworm.util.Error.print(e);
		}
	}

	public static MemoryBuffer read(String filename) {
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(dir + "/" + filename, "r");
			MemoryBuffer unsafeBuffer = new MemoryBuffer((int) raf.length());
			raf.read(unsafeBuffer.getBuffer());
			raf.close();
			return unsafeBuffer;
		} catch (Throwable e) {
			fatworm.util.Error.print(e);
		}
		return null;
	}

	public static void setHomeDir(String folder) {
		dir = folder;
	}
}
//package fatworm.files;
//
//import java.io.RandomAccessFile;
//import java.nio.ByteBuffer;
//import java.nio.channels.FileChannel;
//
//public class Disk {
//	
//	public static String dir=".";
//	
//	public static void write(String filename, MemoryBuffer buffer){
//		RandomAccessFile raf = null;
//		try{
//			raf = new RandomAccessFile(dir+"/"+filename, "rw");
//			FileChannel channel = raf.getChannel(); 
//			channel.write(ByteBuffer.wrap(buffer.getBuffer()));
//			channel.close();
//			raf.close();
//		}catch(Throwable e){
//			fatworm.util.Error.print(e);
//		}
//	}
//	
//	public static void appendFile(String filename, int add, MemoryBuffer buffer){
//		RandomAccessFile raf = null;
//		try{
//			raf = new RandomAccessFile(dir+"/"+filename, "rw");
//			FileChannel channel = raf.getChannel(); 
//			ByteBuffer sizeBuffer=ByteBuffer.wrap(new byte[] {
//		            (byte)add,
//		            (byte)(add >> 8),
//		            (byte)(add >> 16),
//		            (byte)(add >> 24)});
//			while(sizeBuffer.hasRemaining())
//				channel.write(sizeBuffer);
//			
//			channel.position(channel.size());
//			ByteBuffer addBuffer=ByteBuffer.wrap(buffer.getBuffer());
//			while(addBuffer.hasRemaining())
//				channel.write(addBuffer);
//			channel.close();
//			raf.close();
//		}catch(Throwable e){
//			fatworm.util.Error.print(e);
//		}
//	}
//	
//	public static MemoryBuffer read(String filename){
//		RandomAccessFile raf=null;
//		try{
//			raf = new RandomAccessFile(dir+"/"+filename, "r");
//			MemoryBuffer unsafeBuffer = new MemoryBuffer((int) raf.length());
//			raf.read(unsafeBuffer.getBuffer());
//			raf.close();
//			return unsafeBuffer;
//		}catch(Throwable e){
//			fatworm.util.Error.print(e);
//		}
//		return null;
//	}
//
//	public static void setHomeDir(String folder) {
//		dir=folder;
//	}
//}
