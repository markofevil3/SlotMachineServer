package com.yna.game.common;

import java.io.IOException;
import java.sql.Timestamp;

import org.apache.log4j.Logger;

public class Util {
	static Logger logger = Logger.getLogger(Util.class);
	
	static String[] characters = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", 
		   "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", 
		   "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
	
	public static void log(Object message) {
		System.out.println(message);
		logger.debug(message);
		Logger.getLogger("Extensions").info(message);
	}
	
	public static Boolean IsNullOrEmpty(String str) {
		return str.equals("") || str.equals(null);
	}
	
	public static String generateRandomString(int length) {
		String secret = "";
		
		for (int i = 0; i < length; i++) {
			int rand = (int)(Math.random() * characters.length);
			secret += characters[rand];
		}
		
		return secret;
	}
	
	public static String StringFromByteArray(byte[] bytes) {
		String output = "";
		
		try {
			output = new String(bytes, "UTF-8");
		} catch (IOException exception) {
			log("StringFromByteArray:Exception:" + exception.toString());
		}
		
		return output;
	}
	
	public static byte[] StringToBytesArray(String input) {
		byte[] output = new byte[input.length()];
		
		try {
			output = input.getBytes("UTF-8");
		} catch (IOException exception) {
			log("StringToByteArray:Exception:" + exception.toString());
		}
		
		return output;
	}
	
	public static Timestamp ConvertStringToTimestamp(String timeString) {
		return Timestamp.valueOf(timeString);
	}
	
	public static void StringToIntArray(String input, int[] fillArr) {
		String[] split = input.trim().split(",");
		for (int i = 0; i < fillArr.length; i++) {
			if (split.length - 1 >= i) {
				fillArr[i] = Integer.parseInt(split[i].trim());
			}
		}
		split = null;
	}
	
	public static String IntArrayToString(int[] arr) {
		String st = "";
		for (int i = 0; i < arr.length; i++) {
			if (i != arr.length - 1) {
				st += arr[i] + ",";
			} else {
				st += arr[i];
			}
		}
		return st;
	}
}