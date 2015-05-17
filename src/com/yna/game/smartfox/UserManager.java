package com.yna.game.smartfox;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.smartfoxserver.bitswarm.sessions.Session;
import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.api.ISFSApi;
import com.smartfoxserver.v2.api.ISFSBuddyApi;
import com.smartfoxserver.v2.buddylist.BuddyVariable;
import com.smartfoxserver.v2.buddylist.SFSBuddyVariable;
import com.smartfoxserver.v2.db.IDBManager;
import com.smartfoxserver.v2.entities.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;











import com.yna.game.common.ErrorCode;
import com.yna.game.common.GameConstants;
import com.yna.game.common.Util;

public class UserManager {
	
	private static ConcurrentHashMap<String, JSONObject> onlineUsers = new ConcurrentHashMap<String, JSONObject>();
	private static JSONArray topRichers = null;
	private static Date topRichersLastUpdate;
//	private static final int NEW_USER_CASH = 100000;
//	private static final int LEADERBOARD_UPDATE_INTERVAL = 1800; // seconds
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static ISFSBuddyApi buddyApi;

//	private static final int LEADERBOARD_NUMB_USERS = 20;
//	private static final int DAILY_REWARD_MINS = 5;
//	private static final int DAILY_REWARD_CASH = 50000;
	
	// Add user to cache list
	public static void addUser(String username, JSONObject user) {
		onlineUsers.put(username, user);
		Util.log("onlineUsers Count: " + onlineUsers.size());
	}
	
	public static JSONObject verifyFBUser(String fbID, JSONObject jsonData, Session session, ISFSApi sfsApi) {
		try {
			// find user in database
			JSONObject user = null;
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
			  		selectStatement = connection.prepareStatement("SELECT * FROM user WHERE facebookId=?");
			  		selectStatement.setString(1, fbID);
			      // Execute query
				    selectResultSet = selectStatement.executeQuery();
						if (selectResultSet.first()) {
							user = new JSONObject();
							createUserJSONData(user, selectResultSet);
						}
			    }
		    }
		    // facebookId was not found
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
			// create new user using FB ID
			String guestId = jsonData.getString("guestId");
			if (user == null && (guestId == null || guestId.isEmpty())) {
				JSONObject data = new JSONObject();
				int error = registerUser(fbID, jsonData, true);
				jsonData.put("isRegister", true);
				data.put(ErrorCode.PARAM, error);
				data.put("user", jsonData);
				return data;
			} else if (user == null && !guestId.isEmpty()) {
				JSONObject guest = getUser(guestId);
				if (guest == null) {
					JSONObject data = new JSONObject();
					int error = registerUser(fbID, jsonData, true);
					jsonData.put("isRegister", true);
					data.put(ErrorCode.PARAM, error);
					data.put("user", jsonData);
					return data;
				} else {
					if (guest.getString("facebookId").isEmpty()) {
						user = guest;
						user.put("facebookId", fbID);
						user.put("displayName", jsonData.getString("displayName"));
						user.put("email", jsonData.getString("email"));
						user.put("avatar", jsonData.getString("avatar"));
					}
				}
			}
			// Found user in DB
			JSONObject data = new JSONObject();
			data.put(ErrorCode.PARAM, ErrorCode.User.NULL);
			user.put("isRegister", true);
			data.put("user", user);
			SimpleDateFormat sdfDate = new SimpleDateFormat(DATE_FORMAT);
			user.put("lastLogin", sdfDate.format(new Date()));
			// add user to online list if login successed
  		addUser(user.getString("username"), user);
			return data;
		} catch (Exception exception) {
			Util.log("UserManager verifyUser JSONObject Error:" + exception.toString());
		}
		Util.log("UserManager Cant verifyUser");
		return null;
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
	
	public static void updatePlayerCash(JSONObject userData, int updateVal) {
		try {
			int newVal = Math.max(0 ,userData.getInt("cash") + updateVal);
			userData.put("cash", newVal);
		} catch (JSONException e) {
			Util.log("UserManager DeductUserCash JSONObject Error:" + e.toString());
		}
	}
	
	public static void updatePlayerCashAndKill(String username, int addCash, int addKill) {
		try {
			JSONObject userData = getOnlineUser(username);
			int newVal = Math.max(0 ,userData.getInt("cash") + addCash);
			userData.put("cash", newVal);
			userData.put("bossKill", userData.getInt("bossKill") + addKill);
			// TEST CODE - update buddy variable when player killed a boss
			setBuddyVariables(SmartFoxServer.getInstance().getUserManager().getUserByName(userData.getString("username")), userData, false);
		} catch (JSONException e) {
			Util.log("UserManager DeductUserCash JSONObject Error:" + e.toString());
		}
	}
	
	public static long updateLastClaimDailyTime(String username) {
		try {
			JSONObject userData = getOnlineUser(username);
			long crtTime = System.currentTimeMillis();
			userData.put("lastDaily", crtTime);
			return System.currentTimeMillis();
		} catch (JSONException e) {
			Util.log("UserManager DeductUserCash JSONObject Error:" + e.toString());
			return 0;
		}
	}
	
	public static JSONObject claimDailyReward(String username, JSONObject out) {
		try {
			JSONObject userData = getOnlineUser(username);
			long lastClaimedTime = userData.getLong("lastDaily");
			long crtTime = System.currentTimeMillis();

			if (lastClaimedTime == 0 || crtTime - lastClaimedTime >= GameConstants.DAILY_REWARD_MILI) {
				updatePlayerCash(userData, GameConstants.DAILY_REWARD_CASH);
				userData.put("lastDaily", crtTime);
				out.put("lastDaily", crtTime);
				out.put("cash", GameConstants.DAILY_REWARD_CASH);
			} else {
				out.put(ErrorCode.PARAM, ErrorCode.User.CANT_CLAIM_DAILY_YET);
			}
			
			return out;
		} catch (JSONException e) {
			Util.log("UserManager DeductUserCash JSONObject Error:" + e.toString());
		}
		return out;
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
		if (topRichers == null || now.getTime() - topRichersLastUpdate.getTime() >= GameConstants.LEADERBOARD_UPDATE_INTERVAL * 1000) {
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
		  		selectStatement = connection.prepareStatement("SELECT username, displayName, avatar, cash, bossKill, totalWin, biggestWin FROM user ORDER BY cash desc limit " + GameConstants.LEADERBOARD_NUMB_USERS);
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
		Util.log("-----------Online Users: " + countOnlineUSer());
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
	
//	public static void addFriend(String username, String fUsername, JSONObject out) throws JSONException {
//		JSONObject user = getOnlineUser(username);
//		if (user != null) {
//			JSONArray friends = user.getJSONArray("friends");
//
//			int friendNumb = friends.length();
//			if (friendNumb == 0) {
//				friends.put(fUsername);
//				out.put(ErrorCode.PARAM, ErrorCode.User.NULL);
//				out.put("fUsername", fUsername);
//			} else {
//				boolean isFriend = false;
//				for (int i = 0; i < friendNumb; i++) {
//					if (friends.getString(i).equals(fUsername)) {
//						isFriend = true;
//					}
//				}
//				if (!isFriend) {
//					friends.put(fUsername);
//					out.put(ErrorCode.PARAM, ErrorCode.User.NULL);
//					out.put("fUsername", fUsername);
//				} else {
//					out.put(ErrorCode.PARAM, ErrorCode.User.ALREADY_FRIEND);
//				}
//			}
//		} else {
//			out.put(ErrorCode.PARAM, ErrorCode.User.CANT_FIND_USER);
//		}
//	}
	
	public static JSONArray GetUsernamesByFbIds(JSONArray facebookIds) throws JSONException {
		
		Util.log("GetUsernamesByFbIds " + facebookIds.toString());
		if (facebookIds.length() <= 0) {
			return null;
		}
		
		IDBManager dbManager = ClientRequestHandler.zone.getDBManager();
		Connection connection = null;
		PreparedStatement selectStatement = null;
		ResultSet selectResultSet = null;
    try {
	  	// Grab a connection from the DBManager connection pool
	    connection = dbManager.getConnection();
	    // Throw error - cant get any connection
	    if (connection == null) {
				return null;
	    } else {
	    	String statement = "SELECT username FROM user WHERE facebookId IN (";
	    	for (int i = 0; i < facebookIds.length(); i++) {
	    		if (i == facebookIds.length() - 1) {
		    		statement += "?)";
	    		} else {
		    		statement += "?,";
	    		}
	    	}
	  		selectStatement = connection.prepareStatement(statement);
	    	for (int i = 0; i < facebookIds.length(); i++) {
	    		selectStatement.setString(i + 1, facebookIds.getString(i));
	    	}
	      // Execute query
		    selectResultSet = selectStatement.executeQuery();
		    JSONArray usernameArr = new JSONArray();
		    while (selectResultSet.next()) {
		    	usernameArr.put(selectResultSet.getString("username"));
		    }
		    return usernameArr;
	    }
    }
    // Username was not found
    catch (SQLException e) {
  		Util.log("UserManager verifyUser SQLException | JSONException: " + e.toString());
			return null;
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
	
	public static int countOnlineUSer() {
		return onlineUsers.size();
	}
	
	public static int registerUser(String username, JSONObject user, boolean isFBRegister) {
		int errorCode = ErrorCode.User.NULL;
		// add default data to new user
		try {
			user.put("cash", GameConstants.NEW_USER_CASH);
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
	    	if (!isFBRegister) {
		  		selectStatement = connection.prepareStatement("SELECT username FROM user WHERE username=?");
		  		selectStatement.setString(1, username);
		      // Execute query
			    selectResultSet = selectStatement.executeQuery();
					// Verify that no record was found
			    if (!selectResultSet.first()) {
						// Create user and save to db
			  		insertStatement = connection.prepareStatement("INSERT INTO user(username, password, displayName, cash) VALUES (?, ?, ?, ?)");
			  		insertStatement.setString(1, username);
				    insertStatement.setString(2, user.getString("password"));
				    insertStatement.setString(3, user.getString("displayName"));
				    insertStatement.setInt(4, user.getInt("cash"));
			      // Execute query
				    insertStatement.executeUpdate();
					} else {
						// User exist - throw exception
			  		errorCode = ErrorCode.User.USER_EXIST;
					}
	    	} else {
	    		Util.log("UserManager registerUser using Facebook " + username);
	    		// Is Facebook register
					// Create user and save to db
		  		insertStatement = connection.prepareStatement("INSERT INTO user(username, password, displayName, cash, email, avatar, facebookId) VALUES (?, ?, ?, ?, ?, ?, ?)");
		  		insertStatement.setString(1, username);
			    insertStatement.setString(2, user.getString("password"));
			    insertStatement.setString(3, user.getString("displayName"));
			    insertStatement.setInt(4, user.getInt("cash"));
			    insertStatement.setString(5, user.getString("email"));
			    insertStatement.setString(6, user.getString("avatar"));
			    insertStatement.setString(7, username);
		      // Execute query
			    insertStatement.executeUpdate();
	    	}
	  		selectStatement = connection.prepareStatement("SELECT * FROM user WHERE username=?");
	  		selectStatement.setString(1, username);
	      // Execute query
		    selectResultSet = selectStatement.executeQuery();
				if (selectResultSet.first()) {
			    createUserJSONData(user, selectResultSet);
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
  		saveUserToDB(username);
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
																																	+ "facebookId=?, bossKill=?, totalWin=?, biggestWin=?, lastClaimedDaily=? WHERE username=?");
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
			updateStatement.setLong(12, user.getLong("lastDaily"));
			updateStatement.setString(13, username);
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
			user.put("lastDaily", userResultSet.getLong("lastClaimedDaily"));
		} catch (JSONException e) {
  		Util.log("UserManager createUserJSONData JSONException: " + e.toString());
		} catch (SQLException e) {
  		Util.log("UserManager createUserJSONData SQLException: " + e.toString());
		}
		return user;
	}
		
	public static void setBuddyVariables(User player, JSONObject jsonData, boolean shouldInitBuddyList) {
		try {
			if (player == null) {
				Util.log("setBuddyVariables:player == null");

				return;
			}
			Util.log("setBuddyVariables:player " + player.getVariable("displayName") + " " + player.getVariable("cash").getIntValue());

			if (buddyApi == null) {
				buddyApi = SmartFoxServer.getInstance().getAPIManager().getBuddyApi();
			}
			if (shouldInitBuddyList) {
				buddyApi.initBuddyList(player, false);
			}
			List<BuddyVariable> vars = new ArrayList<BuddyVariable>();
			vars.add( new SFSBuddyVariable("displayName", player.getVariable("displayName").getStringValue()));
			vars.add( new SFSBuddyVariable("cash", player.getVariable("cash").getIntValue()));
			vars.add( new SFSBuddyVariable("facebookId", jsonData.getString("facebookId")));
			vars.add( new SFSBuddyVariable("avatar", jsonData.getString("avatar")));
			vars.add( new SFSBuddyVariable("$cash", player.getVariable("cash").getIntValue()));
			vars.add( new SFSBuddyVariable("$displayName", player.getVariable("displayName").getStringValue()));
			vars.add( new SFSBuddyVariable("$facebookId", jsonData.getString("facebookId")));
			vars.add( new SFSBuddyVariable("$avatar", jsonData.getString("avatar")));
			buddyApi.setBuddyVariables(player, vars, true, false);
		} catch (Exception exception) {
			Util.log("setBuddyVariables:Exception:" + exception.toString());
		}
	}
}
