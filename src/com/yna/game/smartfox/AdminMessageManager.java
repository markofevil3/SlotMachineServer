package com.yna.game.smartfox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.yna.game.common.Util;

public class AdminMessageManager {
	
	public static final String ADMIN_MESSAGES_FILE_PATH = "/data/AdminMessages.txt";

	private static final int TO_ALL_USER = 0; // message is sent to all user
	private static final int TO_SPECIFIC_USERS = 1; // message is sent to all user
	
	private static List<AdminMessage> adminMessages = new ArrayList<AdminMessage>();
	
	// TO DO: get list available message from DB to cached list - should be sort by createdAt
	public static void init() {
		BufferedReader bufferedReader;
		try {
			bufferedReader = new BufferedReader(new FileReader(System.getProperty("user.dir") + ADMIN_MESSAGES_FILE_PATH));
			StringBuilder stringBuilder = new StringBuilder();
			String line = bufferedReader.readLine();
			while (line != null) {
				stringBuilder.append(line);
				line = bufferedReader.readLine();
			}
			bufferedReader.close();
			line = stringBuilder.toString();
			if (!line.isEmpty()) {
				JSONArray messages = new JSONArray(stringBuilder.toString());
				if (messages.length() > 0) {
					// TO DO: process data
					JSONObject mesData;
					for (int i = 0; i < messages.length(); i++) {
						mesData = messages.getJSONObject(i);
						AdminMessage adminMes = new AdminMessage(mesData.getInt("id"), mesData.getJSONObject("message"), mesData.getLong("createdAt"),
																										 mesData.getLong("expiredAt"), mesData.getInt("targetType"), mesData.getString("usernames"));
						switch(adminMes.targetType) {
							case TO_ALL_USER: // Send to all user
								Util.log("AdminMessageManager - Found message : id=" + adminMes.id + "| message=" + adminMes.message);
								// TO DO: check expired message
								UserManager.addAdminMessageToOnlineUsers(adminMes.message, adminMes.createdAt);
								adminMessages.add(adminMes);
								break;
							case TO_SPECIFIC_USERS: // Send to specific users
								for (int j = 0; j < adminMes.usernames.length; j++){
									UserManager.addAdminMessageToThisUser(adminMes.usernames[j], adminMes.message, adminMes.createdAt);
								}
								break;
						}
					}
				}
			}
			// clear messages after processed
      BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + ADMIN_MESSAGES_FILE_PATH));
      bufferWriter.write("");
      bufferWriter.flush();
      bufferWriter.close();
		} catch (FileNotFoundException e) {
			Util.log("AdminMessageManager - Init FileNotFoundException: " + e.toString());
		} catch (IOException e) {
			Util.log("AdminMessageManager - Init IOException: " + e.toString());
		} catch (JSONException e) {
			Util.log("AdminMessageManager - Init JSONException: " + e.toString());
		}
	}
	
	public static List<AdminMessage> getNeedToAddMessages(long lastAddedTime) {
		List<AdminMessage> mes = new ArrayList<AdminMessage>();
		AdminMessage adminMes;
		for (int i = 0; i < adminMessages.size(); i++) {
			adminMes = adminMessages.get(i);
			if (adminMes.createdAt > lastAddedTime) {
				mes.add(adminMes);
			}
		}
		return mes;
	}
	
	// TO DO: should parse messageData to jsonArray --- process new added message (send to online user, add to cached list for offline users, remove when expired) 
	public static void proccessMessages(String messageData) {
		
	}
	
	// TO DO: update to check expired time of message to remove
	public void update() {
		
	}
	
	// TO DO: save messages to DB before shutdown server to reload next time
	public static void destroy() {
		
	}
}