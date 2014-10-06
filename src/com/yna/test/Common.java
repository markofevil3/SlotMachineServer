package com.yna.test;


public class Common {
	public static void main(String args[]) {
		String text = "abc.test";
		String[] arr = text.split("\\.");
		System.out.println(arr[arr.length - 1]);
	}
	
}