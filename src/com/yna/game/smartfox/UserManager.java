package com.yna.game.smartfox;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

















import com.yna.game.common.ErrorCode;
import com.yna.game.common.GameConstants;
import com.yna.game.common.Util;
import com.yna.game.slotmachine.models.Command;

public class UserManager {
	
	private static ConcurrentHashMap<String, OnlineUser> onlineUsers = new ConcurrentHashMap<String, OnlineUser>();
	private static JSONArray topRichers = null;
	private static JSONArray topKillers = null;
	private static Date topRichersLastUpdate;
	private static Date topKillersLastUpdate;
//	private static final int NEW_USER_CASH = 100000;
//	private static final int LEADERBOARD_UPDATE_INTERVAL = 1800; // seconds
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static ISFSBuddyApi buddyApi;
	private static ISFSApi sfsApi;
//	private static final int LEADERBOARD_NUMB_USERS = 20;
//	private static final int DAILY_REWARD_MINS = 5;
//	private static final int DAILY_REWARD_CASH = 50000;
	
	public static void init() {
		sfsApi = SmartFoxServer.getInstance().getAPIManager().getSFSApi();
	}
	
	// Add user to cache list
	public static void addUser(String username, JSONObject user) {
		onlineUsers.put(username, new OnlineUser(user));
		Util.log("addUser onlineUsers Count: " + onlineUsers.size());
	}
	
	// TO DO: should check online first???
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
				JSONObject guest = getUser(guestId, false);
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
	
	// verify user when login
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
		OnlineUser ou = onlineUsers.get(username);
		if (ou != null) {
			return ou.jsonData;
		}
		return null;
	}
	
	public static OnlineUser getOnlineUserClass(String username) {
		return onlineUsers.get(username);
	}
	
	public static JSONObject getUser(String username, boolean isViewing) throws JSONException {
		JSONObject user = getOnlineUser(username);
		JSONObject viewData = new JSONObject();
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
		    	if (isViewing) {
		    		// TO DO: select needed fields only
		    		selectStatement = connection.prepareStatement("SELECT * FROM user WHERE username=?");
		    	} else {
			  		selectStatement = connection.prepareStatement("SELECT * FROM user WHERE username=?");
		    	}
		  		selectStatement.setString(1, username);
		      // Execute query
			    selectResultSet = selectStatement.executeQuery();
					if (selectResultSet.first()) {
						if (isViewing) {
							viewData = createUserViewingJSONdata(null, selectResultSet);
						} else {
							user = new JSONObject();
							createUserJSONData(user, selectResultSet);
						}
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
		} else {
			viewData = createUserViewingJSONdata(user, null);
		}
		if (isViewing) {
			return viewData;
		} else {
			return user;
		}
	}
	
	public static void updatePlayerCash(String username, int updateVal) {
		try {
			JSONObject userData = getOnlineUser(username);
			long newVal = Math.max(0 ,userData.getLong("cash") + updateVal);
			userData.put("cash", newVal);
		} catch (JSONException e) {
			Util.log("UserManager DeductUserCash JSONObject Error:" + e.toString());
		}
	}
	
	public static void updatePlayerCash(JSONObject userData, int updateVal) {
		try {
			long newVal = Math.max(0 ,userData.getLong("cash") + updateVal);
			userData.put("cash", newVal);
		} catch (JSONException e) {
			Util.log("UserManager DeductUserCash JSONObject Error:" + e.toString());
		}
	}
	
	public static void updatePlayerCashGemAndKill(String username, int addCash, int addGem, int addKill) {
		try {
			JSONObject userData = getOnlineUser(username);
			if (userData != null) {
				userData.put("cash", Math.max(0, userData.getLong("cash") + addCash));
				userData.put("gem", Math.max(0, userData.getInt("gem") + addGem));
				userData.put("bossKill", userData.getInt("bossKill") + addKill);
				// TEST CODE - update buddy variable when player killed a boss
				setBuddyVariables(SmartFoxServer.getInstance().getUserManager().getUserByName(userData.getString("username")), userData, false);
				Util.log("UserManager updatePlayerCashGemAndKill " + username + " " + addCash + " " + addGem + " " + addKill);
			} else {
				Util.log("UserManager updatePlayerCashGemAndKill user offline:" + username);
			}
		} catch (JSONException e) {
			Util.log("UserManager updatePlayerCashGemAndKill JSONObject Error:" + e.toString());
		}
	}
	
	public static void updatePlayerBossKill(String username, int addKill) {
		try {
			JSONObject userData = getOnlineUser(username);
			if (userData != null) {
				userData.put("bossKill", userData.getInt("bossKill") + addKill);
				setBuddyVariables(SmartFoxServer.getInstance().getUserManager().getUserByName(userData.getString("username")), userData, false);
				Util.log("UserManager updatePlayerBossKill " + username + " " + addKill);
			} else {
				Util.log("UserManager updatePlayerBossKill user offline:" + username);
			}
		} catch (JSONException e) {
			Util.log("UserManager updatePlayerBossKill JSONObject Error:" + e.toString());
		}
	}
	
	public static void updatePlayerCashAndGem(JSONObject userData, int updateCash, int updateGem) {
		try {
			userData.put("cash", Math.max(0 ,userData.getLong("cash") + updateCash));
			userData.put("gem", Math.max(0 ,userData.getInt("gem") + updateGem));
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
	
	public static JSONObject claimInboxReward(String username, int mesType, long createdAt, String fromUsername, JSONObject out) {
		try {
			JSONObject user = getOnlineUser(username);
			JSONArray messages = getUserInbox(user);
			if (messages != null && messages.length() > 0) {
				for (int i = 0; i < messages.length(); i++) {
					JSONObject mes = messages.getJSONObject(i);
					if (mes.getInt("type") == mesType && mes.getLong("createdAt") == createdAt && (fromUsername.isEmpty() || mes.getString("fromUsername") == fromUsername)) {
						int goldVal = mes.getInt("goldVal");
						int gemVal = mes.getInt("gemVal");
						updatePlayerCashAndGem(user, goldVal, gemVal);
						out.put("goldVal", goldVal);
						out.put("gemVal", gemVal);
						out.put(ErrorCode.PARAM, ErrorCode.User.NULL);
						messages.remove(i);
						return out;
					}
				}
				out.put(ErrorCode.PARAM, ErrorCode.User.CANT_FIND_MESSAGE);
			} else {
				out.put(ErrorCode.PARAM, ErrorCode.User.CANT_FIND_MESSAGE);
			}
		} catch (JSONException e) {
			Util.log("UserManager claimInboxReward JSONObject Error:" + e.toString());
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
	
	public static JSONArray getTopKillers() {
		Date now = new Date();
		if (topKillers == null || now.getTime() - topKillersLastUpdate.getTime() >= GameConstants.LEADERBOARD_UPDATE_INTERVAL * 1000) {
  		Util.log("UserManager getTopKillers RELOAD LEADEARBOARD-------------");
  		topKillers = new JSONArray();
			IDBManager dbManager = ClientRequestHandler.zone.getDBManager();
			Connection connection = null;
			PreparedStatement selectStatement = null;
			ResultSet selectResultSet = null;
	    try {
		  	// Grab a connection from the DBManager connection pool
		    connection = dbManager.getConnection();
		    // Throw error - cant get any connection
		    if (connection == null) {
		  		Util.log("UserManager getTopKillers Exception No Connection in pool");
					return new JSONArray();
		    } else {
		  		selectStatement = connection.prepareStatement("SELECT username, displayName, avatar, cash, bossKill, totalWin, biggestWin FROM user where bossKill > 0 ORDER BY bossKill desc limit " + GameConstants.LEADERBOARD_NUMB_USERS);
		      // Execute query
			    selectResultSet = selectStatement.executeQuery();
			    JSONObject user;
			    while (selectResultSet.next()) {
			    	user = new JSONObject();
			    	user.put("username", selectResultSet.getString("username"));
			    	user.put("displayName", selectResultSet.getString("displayName"));
			    	user.put("cash", selectResultSet.getLong("cash"));
			    	user.put("bossKill", selectResultSet.getInt("bossKill"));
			    	user.put("totalWin", selectResultSet.getInt("totalWin"));
			    	user.put("biggestWin", selectResultSet.getInt("biggestWin"));
			    	topKillers.put(user);
			    }
		    }
	    }
	    catch (SQLException | JSONException e) {
	  		Util.log("UserManager getTopKillers SQLException | JSONException: " + e.toString());
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
			topKillersLastUpdate = now;
		}
		return topKillers;
	}
	
	public static JSONArray getTopRichers() {
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
			    	user.put("cash", selectResultSet.getLong("cash"));
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
	
	public static void markRemoveUser(OnlineUser onlineUserClass) {
		onlineUserClass.SetExpiredTime(System.currentTimeMillis() + GameConstants.REMOVE_CACHE_USER_MILI);
		Util.log("-----------markRemoveUser OnlineUser: " + countOnlineUSer());
	}
	
	public static void saveAndClearExpiredCacheUser() {
		OnlineUser tempUser;
		boolean isExpired;
		for (Map.Entry<String, OnlineUser> map : onlineUsers.entrySet()) {
			tempUser = map.getValue();
			isExpired = tempUser.IsExpired();
			Util.log("+++" + tempUser.username + " expired = " + tempUser.IsExpired() + " shouldSave = " + tempUser.ShouldSaveToDB());
			if (isExpired || tempUser.ShouldSaveToDB()) {
				saveUserToDB(map.getValue(), false);
				if (isExpired && sfsApi.getUserByName(tempUser.username) == null) {
					onlineUsers.remove(map.getKey());
				}
			}
		}
		Util.log("-----------clearExpiredCacheUser Online Users: " + countOnlineUSer());
	}
	
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
			  		insertStatement = connection.prepareStatement("INSERT INTO user(username, password, displayName, cash, inboxMes) VALUES (?, ?, ?, ?, ?)");
			  		insertStatement.setString(1, username);
				    insertStatement.setString(2, user.getString("password"));
				    insertStatement.setString(3, user.getString("displayName"));
				    insertStatement.setLong(4, user.getLong("cash"));
				    insertStatement.setString(5, "[]");
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
		  		insertStatement = connection.prepareStatement("INSERT INTO user(username, password, displayName, cash, email, avatar, facebookId, inboxMes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
		  		insertStatement.setString(1, username);
			    insertStatement.setString(2, user.getString("password"));
			    insertStatement.setString(3, user.getString("displayName"));
			    insertStatement.setLong(4, user.getLong("cash"));
			    insertStatement.setString(5, user.getString("email"));
			    insertStatement.setString(6, user.getString("avatar"));
			    insertStatement.setString(7, username);
			    insertStatement.setString(8, "[]");
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
  		saveUserToDB(username, false);
    }
		return errorCode;
	}
	
	public static JSONArray getUserInbox(String username) {
		return getUserInbox(getOnlineUser(username));
	}
	
	public static JSONArray getUserInbox(JSONObject user) {
		try {
			if (user != null) {
				if (user.has("inboxMes")) {
					JSONArray messages = user.getJSONArray("inboxMes");
					if (messages != null && messages.length() > 0) {
						long lastMesTime = messages.getJSONObject(messages.length() - 1).getLong("createdAt");
						if (lastMesTime > user.getLong("lastReadInboxTime")) {
							user.put("lastReadInboxTime", lastMesTime);
						}
					}
					return messages;
				}
			}
		} catch (JSONException e) {
  		Util.log("UserManager getUserInbox JSONException:" + e.toString());
		}
		return null;
	}
	
	// Add message to user inbox offline and online
	public static void addAdminMessageToThisUser(String username, JSONObject message, long createdAt) {
		JSONObject user = getOnlineUser(username);
		if (user != null) {
  		Util.log("UserManager addAdminMessageToThisUser ONLINE " + username);
			addMessageToUserInbox(user, message);
		} else{
  		Util.log("UserManager addAdminMessageToThisUser OFFLINE " + username);
			IDBManager dbManager = ClientRequestHandler.zone.getDBManager();
			Connection connection = null;
			PreparedStatement updateStatement = null;
			try {
				connection = dbManager.getConnection();
				
				updateStatement = connection.prepareStatement("UPDATE user SET inboxMes = IF(inboxMes IS NULL OR inboxMes='' OR inboxMes = '[]', ?, CONCAT(SUBSTR(inboxMes, 1 , CHAR_LENGTH(inboxMes) - 1), ?)),"
																											+ "lastAdminMesTime=?, lastInboxTime= IF(lastInboxTime > ?, lastInboxTime, ?) WHERE username=?");
				
				updateStatement.setString(1, "[" + message.toString() + "]");
				updateStatement.setString(2, "," + message.toString() + "]");
				updateStatement.setLong(3, message.getLong("createdAt"));
				updateStatement.setLong(4, message.getLong("createdAt"));
				updateStatement.setLong(5, createdAt);
				updateStatement.setString(6, username);
		    // Execute query
				updateStatement.executeUpdate();
			} catch (SQLException | JSONException e) {
	  		Util.log("UserManager addMessageToUserInbox String SQLException | JSONException: " + e.toString());
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
		  		Util.log("UserManager addMessageToUserInbox String 2 SQLException: " + e.toString());
				}
			}
		}
	}
	
	// add message to online user inbox
	public static void addMessageToUserInbox(JSONObject user, JSONObject message) {
		try {
			long mesCreatedAt = message.getLong("createdAt");
			long currentInboxTime = user.getLong("lastInboxTime");
			if (user.has("inboxMes") && user.getJSONArray("inboxMes") != null) {
				user.getJSONArray("inboxMes").put(message);
				if (currentInboxTime < mesCreatedAt) {
					user.put("lastInboxTime", mesCreatedAt);
					currentInboxTime = mesCreatedAt;
				}
			} else {
				user.put("inboxMes", new JSONArray().put(message));
			}
  		User userToPushNotice = sfsApi.getUserByName(user.getString("username"));
  		if (userToPushNotice != null) {
  			ISFSObject params = new SFSObject();
  			JSONObject jsonData = new JSONObject();
  			jsonData.put("lastInboxTime", currentInboxTime);
  			params.putByteArray("jsonData", Util.StringToBytesArray(jsonData.toString()));
  			sfsApi.sendExtensionResponse(Command.PUSH_USER_NOTICES, params, sfsApi.getUserByName(user.getString("username")), null, false);
  		}
		} catch (JSONException e) {
  		Util.log("UserManager addMessageToUserInbox JSONException:" + e.toString());
		}
	}
	
	// add admin message to all online user 
	public static void addAdminMessageToOnlineUsers(JSONObject message, long createdAt) {
		try {
			JSONObject jsonData;
			for (Map.Entry<String, OnlineUser> map : onlineUsers.entrySet()) {
				jsonData = map.getValue().jsonData;
				if (jsonData.getLong("lastAdminMesTime") < createdAt) {
					jsonData.put("lastAdminMesTime", createdAt);
					addMessageToUserInbox(jsonData, message);
		  		Util.log("UserManager addMessageToOnlineUsers user:" + map.getKey());
				}
			}
		} catch (JSONException e) {
  		Util.log("UserManager addMessageToOnlineUsers JSONException:" + e.toString());
		}
	}
	
	public static void saveUserToDB(OnlineUser onlineUserClass, boolean markAsRemove) {
		IDBManager dbManager = ClientRequestHandler.zone.getDBManager();
		Connection connection = null;
		PreparedStatement updateStatement = null;
		if (onlineUserClass == null || onlineUserClass.jsonData == null) {
  		Util.log("UserManager saveUserToDB CANT FIND USER IN CACHE ");
			return;
		}
		
		JSONObject user = onlineUserClass.jsonData;
		
		try {
			connection = dbManager.getConnection();
			updateStatement = connection.prepareStatement("UPDATE user SET password=?, displayName=?,"
																																	+ "email=?, avatar=?, cash=?, gem=?, lastLogin=?,"
																																	+ "facebookId=?, bossKill=?, totalWin=?, biggestWin=?, lastClaimedDaily=?,"
																																	+ "inboxMes=?, lastInboxTime=?, lastReadInboxTime=?, lastAdminMesTime=?"
																																	+ " WHERE username=?");
			updateStatement.setString(1, user.getString("password"));
			updateStatement.setString(2, user.getString("displayName"));
			updateStatement.setString(3, user.getString("email"));
			updateStatement.setString(4, user.getString("avatar"));
			updateStatement.setLong(5, user.getLong("cash"));
			updateStatement.setInt(6, user.getInt("gem"));
			updateStatement.setTimestamp(7, Util.ConvertStringToTimestamp(user.getString("lastLogin")));
			updateStatement.setString(8, user.getString("facebookId"));
			updateStatement.setInt(9, user.getInt("bossKill"));
			updateStatement.setInt(10, user.getInt("totalWin"));
			updateStatement.setInt(11, user.getInt("biggestWin"));
			updateStatement.setLong(12, user.getLong("lastDaily"));
			updateStatement.setString(13, user.getJSONArray("inboxMes").toString());
			updateStatement.setLong(14, user.getLong("lastInboxTime"));
			updateStatement.setLong(15, user.getLong("lastReadInboxTime"));
			updateStatement.setLong(16, user.getLong("lastAdminMesTime"));
			updateStatement.setString(17, user.getString("username"));
	    // Execute query
			updateStatement.executeUpdate();
			
			// update lastSaveToDB in cache
			onlineUserClass.SetLastSavedToDB(System.currentTimeMillis());
			// mark as remove to be removed in cache
			if (markAsRemove) {
				markRemoveUser(onlineUserClass);
			}
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
	
	public static void saveUserToDB(String username, boolean markAsRemove) {
		OnlineUser onlineUserClass = getOnlineUserClass(username);
		saveUserToDB(onlineUserClass, markAsRemove);
	}
	
	// create jsonData for user to put in cache
	public static JSONObject createUserJSONData(JSONObject user, ResultSet userResultSet) {
		try {
			user.put("username", userResultSet.getString("username"));
			user.put("password", userResultSet.getString("password"));
			user.put("displayName", userResultSet.getString("displayName"));
			user.put("email", userResultSet.getString("email"));
			user.put("avatar", userResultSet.getString("avatar"));
			user.put("cash", userResultSet.getLong("cash"));
			user.put("gem", userResultSet.getInt("gem"));
			user.put("createdAt", userResultSet.getTimestamp("createdAt").toString());
			user.put("lastLogin", userResultSet.getTimestamp("lastLogin").toString());
			user.put("facebookId", userResultSet.getString("facebookId"));
			user.put("bossKill", userResultSet.getInt("bossKill"));
			user.put("totalWin", userResultSet.getInt("totalWin"));
			user.put("biggestWin", userResultSet.getInt("biggestWin"));
			user.put("lastDaily", userResultSet.getLong("lastClaimedDaily"));
			String inboxRaw = userResultSet.getString("inboxMes");
			user.put("inboxMes", (inboxRaw.isEmpty() || inboxRaw == "[]") ? new JSONArray() : new JSONArray(inboxRaw));
			user.put("lastInboxTime", userResultSet.getLong("lastInboxTime"));
			user.put("lastReadInboxTime", userResultSet.getLong("lastReadInboxTime"));
			user.put("lastAdminMesTime", userResultSet.getLong("lastAdminMesTime"));
  		List<AdminMessage> adminMessages = AdminMessageManager.getNeedToAddMessages(user.getLong("lastAdminMesTime"));
			int length = adminMessages.size();
			if (length > 0) {
				for (int i = 0; i < length; i++) {
					AdminMessage adminMes = adminMessages.get(i);
					addMessageToUserInbox(user, adminMes.message);
					if (user.getLong("lastAdminMesTime") < adminMes.createdAt) {
						user.put("lastAdminMesTime", adminMes.createdAt);
					}
				}
			}
		} catch (JSONException e) {
  		Util.log("UserManager createUserJSONData JSONException: " + e.toString());
		} catch (SQLException e) {
  		Util.log("UserManager createUserJSONData SQLException: " + e.toString());
		}
		return user;
	}
	
	// create jsonData for user to view profile only
	public static JSONObject createUserViewingJSONdata(JSONObject user, ResultSet userResultSet) {
		JSONObject viewData = new JSONObject();
		try {
			if (user == null) {
				viewData.put("username", userResultSet.getString("username"));
				viewData.put("displayName", userResultSet.getString("displayName"));
				viewData.put("email", userResultSet.getString("email"));
				viewData.put("avatar", userResultSet.getString("avatar"));
				viewData.put("cash", userResultSet.getLong("cash"));
				viewData.put("gem", userResultSet.getInt("gem"));
				viewData.put("createdAt", userResultSet.getTimestamp("createdAt").toString());
				viewData.put("lastLogin", userResultSet.getTimestamp("lastLogin").toString());
				viewData.put("facebookId", userResultSet.getString("facebookId"));
				viewData.put("bossKill", userResultSet.getInt("bossKill"));
				viewData.put("totalWin", userResultSet.getInt("totalWin"));
				viewData.put("biggestWin", userResultSet.getInt("biggestWin"));
			} else {
				viewData.put("username", user.getString("username"));
				viewData.put("displayName", user.getString("displayName"));
				viewData.put("email", user.getString("email"));
				viewData.put("avatar", user.getString("avatar"));
				viewData.put("cash", user.getLong("cash"));
				viewData.put("gem", user.getInt("gem"));
				viewData.put("createdAt", user.getString("createdAt"));
				viewData.put("lastLogin", user.getString("lastLogin"));
				viewData.put("facebookId", user.getString("facebookId"));
				viewData.put("bossKill", user.getInt("bossKill"));
				viewData.put("totalWin", user.getInt("totalWin"));
				viewData.put("biggestWin", user.getInt("biggestWin"));
			}
		} catch (JSONException e) {
  		Util.log("UserManager createUserViewingJSONdata JSONException: " + e.toString());
		} catch (SQLException e) {
  		Util.log("UserManager createUserViewingJSONdata SQLException: " + e.toString());
		}
		return viewData;
	}
	
	public static void setBuddyVariables(User player, JSONObject jsonData, boolean shouldInitBuddyList) {
		try {
			if (player == null) {
				Util.log("setBuddyVariables:player == null");

				return;
			}
			Util.log("setBuddyVariables:player " + player.getVariable("displayName") + " " + player.getVariable("cash").getDoubleValue().longValue());

			if (buddyApi == null) {
				buddyApi = SmartFoxServer.getInstance().getAPIManager().getBuddyApi();
			}
			if (shouldInitBuddyList) {
				buddyApi.initBuddyList(player, false);
			}
			List<BuddyVariable> vars = new ArrayList<BuddyVariable>();
			vars.add( new SFSBuddyVariable("displayName", player.getVariable("displayName").getStringValue()));
			vars.add( new SFSBuddyVariable("cash", player.getVariable("cash").getDoubleValue().longValue()));
			vars.add( new SFSBuddyVariable("facebookId", jsonData.getString("facebookId")));
			vars.add( new SFSBuddyVariable("avatar", jsonData.getString("avatar")));
			vars.add( new SFSBuddyVariable("$cash", player.getVariable("cash").getDoubleValue().longValue()));
			vars.add( new SFSBuddyVariable("$displayName", player.getVariable("displayName").getStringValue()));
			vars.add( new SFSBuddyVariable("$facebookId", jsonData.getString("facebookId")));
			vars.add( new SFSBuddyVariable("$avatar", jsonData.getString("avatar")));
			buddyApi.setBuddyVariables(player, vars, true, false);
		} catch (Exception exception) {
			Util.log("setBuddyVariables:Exception:" + exception.toString());
		}
	}
}
