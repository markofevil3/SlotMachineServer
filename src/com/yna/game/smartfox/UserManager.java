package com.yna.game.smartfox;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.smartfoxserver.bitswarm.sessions.Session;
import com.smartfoxserver.v2.api.ISFSApi;
import com.smartfoxserver.v2.api.SFSApi;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.exceptions.SFSVariableException;
import com.yna.game.common.ErrorCode;
import com.yna.game.common.Util;

public class UserManager {
	
	private static ConcurrentHashMap<String, JSONObject> onlineUsers = new ConcurrentHashMap<String, JSONObject>();
	
	// Add user to cache list
	public static void addUser(String username, JSONObject user) {
		onlineUsers.put(username, user);
		Util.log("onlineUsers Count: " + onlineUsers.size());
	}
	
	// verify user when register
	public static JSONObject verifyUser(String username, String password, Session session, ISFSApi sfsApi) {
		try {
			// find user in cache
			JSONObject user = getOnlineUser(username);
			if (user == null) {
				// TO DO: Find user in database
				Util.log("UserManager verifyUser NULL");
			}
			if (user == null) {
				JSONObject error = new JSONObject();
				error.put(ErrorCode.PARAM, ErrorCode.User.USER_NOT_EXIST);
				return error;
			}
			JSONObject data = new JSONObject();
			if (sfsApi.checkSecurePassword(session, user.getString("password"), password)) {
				data.put(ErrorCode.PARAM, ErrorCode.User.NULL);
				data.put("user", user);
				return data;
			} else {
				JSONObject error = new JSONObject();
				error.put(ErrorCode.PARAM, ErrorCode.User.PASSWORD_NOT_MATCH);
				return error;
			}
		} catch (Exception exception) {
			Util.log("UserManager verifyUser JSONObject Error:" + exception.toString());
		}
		Util.log("UserManager Cant verifyUser");
		return null;
	}
	
	// Get user from cache list
	public static JSONObject getOnlineUser(String username) {
		return onlineUsers.get(username);
	}
	
	public static JSONObject getUser(String username) {
		JSONObject user = onlineUsers.get(username);
		if (user == null) {
			// TO DO: Find user in database
		}
		return user;
	}
	
	public static void updatePlayerCash(String username, int updateVal) {
		try {
			JSONObject userData = getOnlineUser(username);
			int newVal = Math.max(0 ,userData.getInt("cash") + updateVal);
			userData.put("cash", newVal);
		} catch (JSONException e) {
			Util.log("UserManager DeductUserCash JSONObject Error:" + e.toString());
		}
	}
	
	// FAKE for leaderboard - should have new function for leaderboard
	public static JSONArray getAllUsers() {
		JSONArray users = new JSONArray();
		try {
			for (JSONObject user : onlineUsers.values()) {
				// FAKE win match number
				user.put("winMatchNumb", new Random().nextInt(100));
				users.put(user);
			}
		} catch (Exception exception) {
			Util.log("UserManager getAllUsers JSONObject Error:" + exception.toString());
		}
		return users;
	}
	
	// Remove user from cache list
	public static void removeUser(String username) {
		onlineUsers.remove(username);
	}

	// Get multiple users by list usernames (online and offline)
	public static JSONArray getUsers(JSONArray usernameList) throws JSONException {
		String username;
		JSONObject tempUser;
		JSONObject user;
		JSONArray friends = new JSONArray();
		JSONArray offlineUsers = new JSONArray();
		// Get online users from cache
		for (int i = 0; i < usernameList.length(); i++) {
			username = usernameList.getString(i);
			tempUser = getOnlineUser(username);
			user = new JSONObject();
			user.put("username", tempUser.get("username"));
			user.put("displayName", tempUser.get("displayName"));
			user.put("cash", tempUser.getInt("cash"));
			if (user != null) {
				friends.put(user);
			} else {
				offlineUsers.put(username);
			}
		}
		// TO DO: get offline user from database

		return friends;
	}
	
	public static void addFriend(String username, String fUsername, JSONObject out) throws JSONException {
		JSONObject user = getOnlineUser(username);
		if (user != null) {
			JSONArray friends = user.getJSONArray("friends");

			int friendNumb = friends.length();
			if (friendNumb == 0) {
				friends.put(fUsername);
				out.put(ErrorCode.PARAM, ErrorCode.User.NULL);
				out.put("fUsername", fUsername);
			} else {
				boolean isFriend = false;
				for (int i = 0; i < friendNumb; i++) {
					if (friends.getString(i).equals(fUsername)) {
						isFriend = true;
					}
				}
				if (!isFriend) {
					friends.put(fUsername);
					out.put(ErrorCode.PARAM, ErrorCode.User.NULL);
					out.put("fUsername", fUsername);
				} else {
					out.put(ErrorCode.PARAM, ErrorCode.User.ALREADY_FRIEND);
				}
			}
		} else {
			out.put(ErrorCode.PARAM, ErrorCode.User.CANT_FIND_USER);
		}
	}
	
	public static int countOnlineUSer() {
		return onlineUsers.size();
	}
	
	public static boolean registerUser(String username, JSONObject user) {
		//TO DO: check exist name and add user to DB
		// add default data to new user
		try {
			user.put("friends", new JSONArray());
		} catch (Exception exception) {
			Util.log("UserManager registerUser JSONObject Error:" + exception.toString());
		}
		// add user to online list if register successed
		addUser(username, user);
		return true;
	}
}
