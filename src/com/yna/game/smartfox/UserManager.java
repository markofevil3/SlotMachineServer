package com.yna.game.smartfox;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.smartfoxserver.bitswarm.sessions.Session;
import com.smartfoxserver.v2.api.ISFSApi;
import com.smartfoxserver.v2.db.IDBManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;








import com.yna.game.common.ErrorCode;
import com.yna.game.common.Util;

public class UserManager {
	
	private static ConcurrentHashMap<String, JSONObject> onlineUsers = new ConcurrentHashMap<String, JSONObject>();
	private static JSONArray topRichers = null;
	private static Date topRichersLastUpdate;
	private static final int NEW_USER_CASH = 100000;
	private static final int LEADERBOARD_UPDATE_INTERVAL = 1800; // seconds
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final int LEADERBOARD_NUMB_USERS = 20;
	
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
				IDBManager dbManager = ClientRequestHandler.zone.getDBManager();
				Connection connection = null;
				PreparedStatement selectStatement = null;
				ResultSet selectResultSet = null;
		    try {
			  	// Grab a connection from the DBManager connection pool
			    connection = dbManager.getConnection();
			    // Throw error - cant get any connection
			    if (connection == null) {
			  		JSONObject error = new JSONObject();
						error.put(ErrorCode.PARAM, ErrorCode.User.UNKNOWN);
						return error;
			    } else {
			  		selectStatement = connection.prepareStatement("SELECT * FROM user WHERE username=?");
			  		selectStatement.setString(1, username);
			      // Execute query
				    selectResultSet = selectStatement.executeQuery();
						if (selectResultSet.first()) {
							user = new JSONObject();
							createUserJSONData(user, selectResultSet);
						}
			    }
		    }
		    // Username was not found
		    catch (SQLException | JSONException e) {
		  		Util.log("UserManager verifyUser SQLException | JSONException: " + e.toString());
		  		JSONObject error = new JSONObject();
					error.put(ErrorCode.PARAM, ErrorCode.User.UNKNOWN);
					return error;
		    }

				finally
				{
					// Return connection to the DBManager connection pool
					try {
						connection.close();
						if (selectStatement != null) {
							selectStatement.close();
						}
						if (selectResultSet != null) {
							selectResultSet.close();
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			if (user == null) {
				JSONObject error = new JSONObject();
				error.put(ErrorCode.PARAM, ErrorCode.User.USER_NOT_EXIST);
				return error;
			}
			JSONObject data = new JSONObject();
			if (sfsApi.checkSecurePassword(session, user.getString("password"), password)) {
				data.put(ErrorCode.PARAM, ErrorCode.User.NULL);
				user.put("isRegister", false);
				data.put("user", user);
				SimpleDateFormat sdfDate = new SimpleDateFormat(DATE_FORMAT);
				user.put("lastLogin", sdfDate.format(new Date()));
				// add user to online list if login successed
	  		addUser(username, user);
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
	
	// TO DO: remove unwanted field in return data 
	public static JSONObject getUser(String username) throws JSONException {
		JSONObject user = onlineUsers.get(username);
		if (user == null) {
			IDBManager dbManager = ClientRequestHandler.zone.getDBManager();
			Connection connection = null;
			PreparedStatement selectStatement = null;
			ResultSet selectResultSet = null;
	    try {
		  	// Grab a connection from the DBManager connection pool
		    connection = dbManager.getConnection();
		    // Throw error - cant get any connection
		    if (connection == null) {
					user = new JSONObject();
		  		user.put(ErrorCode.PARAM, ErrorCode.User.UNKNOWN);
					return user;
		    } else {
		  		selectStatement = connection.prepareStatement("SELECT * FROM user WHERE username=?");
		  		selectStatement.setString(1, username);
		      // Execute query
			    selectResultSet = selectStatement.executeQuery();
					if (selectResultSet.first()) {
						user = new JSONObject();
						createUserJSONData(user, selectResultSet);
					}
		    }
	    }
	    // Username was not found
	    catch (SQLException | JSONException e) {
	  		Util.log("UserManager verifyUser SQLException | JSONException: " + e.toString());
	  		user.put(ErrorCode.PARAM, ErrorCode.User.UNKNOWN);
				return user;
	    }

			finally
			{
				// Return connection to the DBManager connection pool
				try {
					connection.close();
					if (selectStatement != null) {
						selectStatement.close();
					}
					if (selectResultSet != null) {
						selectResultSet.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
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
//	public static JSONArray getAllUsers() {
//		JSONArray users = new JSONArray();
//		try {
//			for (JSONObject user : onlineUsers.values()) {
//				// FAKE win match number
//				user.put("winMatchNumb", new Random().nextInt(100));
//				users.put(user);
//			}
//		} catch (Exception exception) {
//			Util.log("UserManager getAllUsers JSONObject Error:" + exception.toString());
//		}
//		return users;
//	}
	
	// TO DO: get leaderboard by type
	public static JSONArray getLeaderboardUsers() {
		Date now = new Date();
		if (topRichers == null || now.getTime() - topRichersLastUpdate.getTime() >= LEADERBOARD_UPDATE_INTERVAL * 1000) {
  		Util.log("UserManager getLeaderboardUsers RELOAD LEADEARBOARD-------------");
			topRichers = new JSONArray();
			IDBManager dbManager = ClientRequestHandler.zone.getDBManager();
			Connection connection = null;
			PreparedStatement selectStatement = null;
			ResultSet selectResultSet = null;
	    try {
		  	// Grab a connection from the DBManager connection pool
		    connection = dbManager.getConnection();
		    // Throw error - cant get any connection
		    if (connection == null) {
		  		Util.log("UserManager getLeaderboardUsers Exception No Connection in pool");
					return new JSONArray();
		    } else {
		  		selectStatement = connection.prepareStatement("SELECT username, displayName, avatar, cash, bossKill, totalWin, biggestWin FROM user ORDER BY cash desc limit " + LEADERBOARD_NUMB_USERS);
		      // Execute query
			    selectResultSet = selectStatement.executeQuery();
			    JSONObject user;
			    while (selectResultSet.next()) {
			    	user = new JSONObject();
			    	user.put("username", selectResultSet.getString("username"));
			    	user.put("displayName", selectResultSet.getString("displayName"));
			    	user.put("cash", selectResultSet.getInt("cash"));
			    	user.put("bossKill", selectResultSet.getInt("bossKill"));
			    	user.put("totalWin", selectResultSet.getInt("totalWin"));
			    	user.put("biggestWin", selectResultSet.getInt("biggestWin"));
			    	topRichers.put(user);
			    }
		    }
	    }
	    catch (SQLException | JSONException e) {
	  		Util.log("UserManager getLeaderboardUsers SQLException | JSONException: " + e.toString());
				return new JSONArray();
	    }

			finally
			{
				// Return connection to the DBManager connection pool
				try {
					connection.close();
					if (selectStatement != null) {
						selectStatement.close();
					}
					if (selectResultSet != null) {
						selectResultSet.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			topRichersLastUpdate = now;
		}
		return topRichers;
	}
	
	// Remove user from cache list
	public static void removeUser(String username) {
		onlineUsers.remove(username);
	}

	// Get multiple users by list usernames (online and offline)
//	public static JSONArray getUsers(JSONArray usernameList) throws JSONException {
//		String username;
//		JSONObject tempUser;
//		JSONObject user;
//		JSONArray friends = new JSONArray();
//		JSONArray offlineUsers = new JSONArray();
//		// Get online users from cache
//		for (int i = 0; i < usernameList.length(); i++) {
//			username = usernameList.getString(i);
//			tempUser = getOnlineUser(username);
//			user = new JSONObject();
//			user.put("username", tempUser.get("username"));
//			user.put("displayName", tempUser.get("displayName"));
//			user.put("cash", tempUser.getInt("cash"));
//			if (user != null) {
//				friends.put(user);
//			} else {
//				offlineUsers.put(username);
//			}
//		}
//		// TO DO: get offline user from database
//
//		return friends;
//	}
	
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
	
	public static int registerUser(String username, JSONObject user) {
		int errorCode = ErrorCode.User.NULL;
		// add default data to new user
		try {
			user.put("cash", NEW_USER_CASH);
		} catch (Exception exception) {
			Util.log("UserManager registerUser JSONObject Error:" + exception.toString());
		}

		IDBManager dbManager = ClientRequestHandler.zone.getDBManager();
		Connection connection = null;
		PreparedStatement selectStatement = null;
		ResultSet selectResultSet = null;
		PreparedStatement insertStatement = null;
    try {
	  	// Grab a connection from the DBManager connection pool
	    connection = dbManager.getConnection();
	    // Throw error - cant get any connection
	    if (connection == null) {
	  		errorCode = ErrorCode.User.UNKNOWN;
	    } else {
	  		selectStatement = connection.prepareStatement("SELECT username FROM user WHERE username=?");
	  		selectStatement.setString(1, username);
	      // Execute query
		    selectResultSet = selectStatement.executeQuery();
				// Verify that one record was found
				if (!selectResultSet.first()) {
					// Create user and save to db
		  		insertStatement = connection.prepareStatement("INSERT INTO user(username, password, displayName, cash) VALUES (?, ?, ?, ?)");
		  		insertStatement.setString(1, username);
			    insertStatement.setString(2, user.getString("password"));
			    insertStatement.setString(3, user.getString("displayName"));
			    insertStatement.setInt(4, user.getInt("cash"));
		      // Execute query
			    insertStatement.executeUpdate();
		  		selectStatement = connection.prepareStatement("SELECT * FROM user WHERE username=?");
		  		selectStatement.setString(1, username);
		      // Execute query
			    selectResultSet = selectStatement.executeQuery();
					if (selectResultSet.first()) {
				    createUserJSONData(user, selectResultSet);
			    }

				} else {
					// User exist - throw exception
		  		errorCode = ErrorCode.User.USER_EXIST;
				}
	    }
    }
    // Username was not found
    catch (SQLException | JSONException e) {
  		Util.log("UserManager registerUser SQLException | JSONException: " + e.toString());
  		errorCode = ErrorCode.User.UNKNOWN;
    }

		finally
		{
			// Return connection to the DBManager connection pool
			try {
				connection.close();
				if (selectStatement != null) {
					selectStatement.close();
				}
				if (selectResultSet != null) {
					selectResultSet.close();
				}
				if (insertStatement != null) {
					insertStatement.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		// add user to online list if register successed
    if (errorCode == ErrorCode.User.NULL) {
  		addUser(username, user);
    }
		return errorCode;
	}
	
	public static void saveUserToDB(String username) {
		JSONObject user = getOnlineUser(username);
		IDBManager dbManager = ClientRequestHandler.zone.getDBManager();
		Connection connection = null;
		PreparedStatement updateStatement = null;
		if (user == null) {
  		Util.log("UserManager saveUserToDB CANT FIND USER IN CACHE " + username);
			return;
		}
		try {
			connection = dbManager.getConnection();
			updateStatement = connection.prepareStatement("UPDATE user SET password=?, displayName=?,"
																																	+ "email=?, avatar=?, cash=?, gem=?, lastLogin=?,"
																																	+ "facebookId=?, bossKill=?, totalWin=?, biggestWin=? WHERE username=?");
			updateStatement.setString(1, user.getString("password"));
			updateStatement.setString(2, user.getString("displayName"));
			updateStatement.setString(3, user.getString("email"));
			updateStatement.setString(4, user.getString("avatar"));
			updateStatement.setInt(5, user.getInt("cash"));
			updateStatement.setInt(6, user.getInt("gem"));
			updateStatement.setTimestamp(7, Util.ConvertStringToTimestamp(user.getString("lastLogin")));
			updateStatement.setString(8, user.getString("facebookId"));
			updateStatement.setInt(9, user.getInt("bossKill"));
			updateStatement.setInt(10, user.getInt("totalWin"));
			updateStatement.setInt(11, user.getInt("biggestWin"));
			updateStatement.setString(12, username);
	    // Execute query
			updateStatement.executeUpdate();
		} catch (SQLException | JSONException e) {
  		Util.log("UserManager saveUserToDB SQLException | JSONException: " + e.toString());
  		// TO DO save exception to exception log file
		}
		finally
		{
			// Return connection to the DBManager connection pool
			try {
				if (connection != null) {
					connection.close();
				}
				if (updateStatement != null) {
					updateStatement.close();
				}
			} catch (SQLException e) {
	  		Util.log("UserManager saveUserToDB 2 SQLException: " + e.toString());
			}
		}
	}
	
	public static JSONObject createUserJSONData(JSONObject user, ResultSet userResultSet) {
		try {
			user.put("username", userResultSet.getString("username"));
			user.put("password", userResultSet.getString("password"));
			user.put("displayName", userResultSet.getString("displayName"));
			user.put("email", userResultSet.getString("email"));
			user.put("avatar", userResultSet.getString("avatar"));
			user.put("cash", userResultSet.getInt("cash"));
			user.put("gem", userResultSet.getInt("gem"));
			user.put("createdAt", userResultSet.getTimestamp("createdAt").toString());
			user.put("lastLogin", userResultSet.getTimestamp("lastLogin").toString());
			user.put("facebookId", userResultSet.getString("facebookId"));
			user.put("bossKill", userResultSet.getInt("bossKill"));
			user.put("totalWin", userResultSet.getInt("totalWin"));
			user.put("biggestWin", userResultSet.getInt("biggestWin"));
		} catch (JSONException e) {
  		Util.log("UserManager createUserJSONData JSONException: " + e.toString());
		} catch (SQLException e) {
  		Util.log("UserManager createUserJSONData SQLException: " + e.toString());
		}
		return user;
	}
	
	// TO DO: remove user from cache when offline
}
