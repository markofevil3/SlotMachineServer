package com.yna.game.common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class GameConstants {
	
	public static final String GAMECONSTANTS_FILE_PATH = "/data/GameConstants.txt";
	public static int LEADERBOARD_TOP_RICHER = 0;
	public static int LEADERBOARD_TOP_KILLER = 1;
	
	public static int NEW_USER_CASH = 100000;
	public static int LEADERBOARD_UPDATE_INTERVAL = 1800; // seconds
	public static int LEADERBOARD_NUMB_USERS = 20;
	public static int DAILY_REWARD_MILI = 300000;
	public static int DAILY_REWARD_CASH = 50000;
	public static int INVITE_MESSAGE_EXPIRED_SECONDS = 50;
	public static int LOBBY_MAX_USERS = 3000;
	public static long REMOVE_CACHE_USER_MILI = 60000;
	public static long CLEAR_CACHE_USERS_INTERVAL_MILI = 120000;
	public static long SAVE_TO_DB_INTERVAL_MILI = 300000;

	public static void init() {
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(System.getProperty("user.dir") + GAMECONSTANTS_FILE_PATH));
			String line = bufferedReader.readLine();
			Util.log("==========GameConstants Init==========");
			while (line != null) {
				String[] arr = line.split("=");
				if (arr.length >= 2) {
					switch(arr[0].trim()) {
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
						case "LOBBY_MAX_USERS":
							LOBBY_MAX_USERS = Integer.parseInt(arr[1].trim());
							break;
						case "REMOVE_CACHE_USER_MILI":
							REMOVE_CACHE_USER_MILI = Long.parseLong(arr[1].trim());
							break;
						case "CLEAR_CACHE_USERS_INTERVAL_MILI":
							CLEAR_CACHE_USERS_INTERVAL_MILI = Long.parseLong(arr[1].trim());
							break;
						case "SAVE_TO_DB_INTERVAL_MILI":
							SAVE_TO_DB_INTERVAL_MILI = Long.parseLong(arr[1].trim());
							break;
					}
				}
				Util.log(line);
				line = bufferedReader.readLine();
			}
			bufferedReader.close();
			Util.log("==========END GameConstants Init==========");
		} catch (IOException e) {
			Util.log("GameConstants:IOException:"	+ e.toString());
		}
	}
}
