package utils;

public class Constants {
	public final static String CONNECT = "connect";
	public final static String SET_PATH = "path";
	public final static String EXIT = "exit";
	
	public final static byte SYNC = 0;
	public final static byte SYNC_OK = 1;
	public final static byte SYNC_NOT_OK = 2;
	
	public final static byte DATA = 3;
	public final static byte DONE = 4;
	
	public final static byte CLOSE = 5;
	public final static byte CLOSE_OK = 6;
	
	public final static int chunkLength = 1024 * 1024;
}
