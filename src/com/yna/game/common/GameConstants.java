package com.yna.game.common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class GameConstants {
	
	public static final String DATA_FILE_PATH = "/data/GameConstants.txt";
	public static String test2 = "";
	public static int NEW_USER_CASH = 100000;
	public static int LEADERBOARD_UPDATE_INTERVAL = 1800; // seconds
	public static int LEADERBOARD_NUMB_USERS = 20;
	public static int DAILY_REWARD_MILI = 300000;
	public static int DAILY_REWARD_CASH = 50000;
	public static int INVITE_MESSAGE_EXPIRED_SECONDS = 50;

	public static void Init() {
		try {
			BufferedReader bufferedReader = new BufferedReader(
			new FileReader(System.getProperty("user.dir") + DATA_FILE_PATH));
			String line = bufferedReader.readLine();
			while (line != null) {
				String[] arr = line.split("=");
				if (arr.length >= 2) {
					switch(arr[0].trim()) {
						case "test":
							test2 = arr[1].trim();
							break;
						case "NEW_USER_CASH":
							NEW_USER_CASH = Integer.parseInt(arr[1].trim());
							break;
						case "LEADERBOARD_UPDATE_INTERVAL":
							LEADERBOARD_UPDATE_INTERVAL = Integer.parseInt(arr[1].trim());
							break;
						case "LEADERBOARD_NUMB_USERS":
							LEADERBOARD_NUMB_USERS = Integer.parseInt(arr[1].trim());
							break;
						case "DAILY_REWARD_MILI":
							DAILY_REWARD_MILI = Integer.parseInt(arr[1].trim());
							break;
						case "DAILY_REWARD_CASH":
							DAILY_REWARD_CASH = Integer.parseInt(arr[1].trim());
							break;
						case "INVITE_MESSAGE_EXPIRED_SECONDS":
							INVITE_MESSAGE_EXPIRED_SECONDS = Integer.parseInt(arr[1].trim());
							break;
					}
				}
				line = bufferedReader.readLine();
			}
			bufferedReader.close();
		} catch (IOException e) {
			Util.log("GameConstants:IOException:"	+ e.toString());
		}
	}
}
