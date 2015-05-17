package com.yna.game.smartfox;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.api.ISFSBuddyApi;
import com.smartfoxserver.v2.buddylist.BuddyVariable;
import com.smartfoxserver.v2.buddylist.SFSBuddyVariable;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSConstants;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.exceptions.SFSErrorCode;
import com.smartfoxserver.v2.exceptions.SFSErrorData;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.exceptions.SFSLoginException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import com.smartfoxserver.bitswarm.sessions.Session;
import com.yna.game.common.ErrorCode;
import com.yna.game.common.Util;

public class GambleEventHandler extends BaseServerEventHandler {
	
	@Override
	public void handleServerEvent(ISFSEvent event) throws SFSException {
		JSONObject jsonData;
		User user;
		if (ClientRequestHandler.zone == null) {
			ClientRequestHandler.zone = getParentExtension().getParentZone();
		}
		
		switch (event.getType()) {
		case USER_LOGIN:
			String username = (String) event.getParameter(SFSEventParam.LOGIN_NAME);
			String password = (String) event.getParameter(SFSEventParam.LOGIN_PASSWORD);
			ISFSObject outData = (ISFSObject)event.getParameter(SFSEventParam.LOGIN_OUT_DATA);
			ISFSObject sfsObj = (ISFSObject)event.getParameter(SFSEventParam.LOGIN_IN_DATA);
			int errorCode = 0;
			try {
				jsonData = new JSONObject(Util.StringFromByteArray(sfsObj.getByteArray("jsonData")));
				if (jsonData.has("isRegister") && jsonData.getBoolean("isRegister")) {
					Boolean isGuest = jsonData.getBoolean("isGuest");
					trace("GambleEventHandler : USER_REGISTER " + username + " " + password + " guest:" + isGuest + "#");
					// Register new user
					errorCode = UserManager.registerUser(username, jsonData, false);
					if (errorCode != ErrorCode.User.NULL) {
						JSONObject error = new JSONObject();
						error.put(ErrorCode.PARAM, errorCode);
						outData.putByteArray("jsonData", Util.StringToBytesArray(error.toString()));
						throw new SFSLoginException("REGISTER ERROR: " + errorCode); 
					} else {
						outData.putByteArray("jsonData", Util.StringToBytesArray(jsonData.toString()));
					}
				} else if (jsonData.has("isFBLogin") && jsonData.getBoolean("isFBLogin")) {
					trace("GambleEventHandler : USER_REGISTER FACEBOOK" + username);

					JSONObject data = UserManager.verifyFBUser(username, jsonData, (Session)event.getParameter(SFSEventParam.SESSION), getApi());
					errorCode = data.getInt(ErrorCode.PARAM);
					if (errorCode == ErrorCode.User.NULL) {
						outData.putByteArray("jsonData", Util.StringToBytesArray(data.getJSONObject("user").toString()));
						outData.putUtfString(SFSConstants.NEW_LOGIN_NAME, data.getJSONObject("user").getString("username"));
					} else {
						JSONObject error = new JSONObject();
						error.put(ErrorCode.PARAM, errorCode);
						outData.putByteArray("jsonData", Util.StringToBytesArray(error.toString()));
						throw new SFSLoginException("LOGIN ERROR: " + errorCode); 
					}
				} else {
					trace("GambleEventHandler : USER_LOGIN " + username + " " + password);
					// check user data
					JSONObject data = UserManager.verifyUser(username, password, (Session)event.getParameter(SFSEventParam.SESSION), getApi());
					errorCode = data.getInt(ErrorCode.PARAM);
					if (errorCode == ErrorCode.User.NULL) {
						outData.putByteArray("jsonData", Util.StringToBytesArray(data.getJSONObject("user").toString()));
					} else {
						JSONObject error = new JSONObject();
						error.put(ErrorCode.PARAM, errorCode);
						outData.putByteArray("jsonData", Util.StringToBytesArray(error.toString()));
						throw new SFSLoginException("LOGIN ERROR: " + errorCode); 
					}
				}
			} catch (Exception exception) {
				trace("GambleEventHandler USER_LOGIN JSONObject Error:" + exception.toString());
				jsonData = null;
				// Custom error message send to client
	      SFSErrorData ed = new SFSErrorData(SFSErrorCode.LOGIN_BAD_USERNAME);
	      ed.addParameter(String.valueOf(errorCode));
	      throw new SFSLoginException("%d", ed);
			}
		case USER_JOIN_ZONE:
			user = (User)event.getParameter(SFSEventParam.USER);
			if (user != null) {
				setUserVariables(user, UserManager.getOnlineUser(user.getName()));
			}
			trace("##handleServerEvent - USER_JOIN_ZONE: " + user);
			break;
		case USER_DISCONNECT:
			user = (User)event.getParameter(SFSEventParam.USER);
			trace("------handleServerEvent - USER_DISCONNECT: " + user.getName());
			UserManager.saveUserToDB(user.getName());
			UserManager.removeUser(user.getName());
			break;
		case USER_LOGOUT:
			user = (User)event.getParameter(SFSEventParam.USER);
			trace("------handleServerEvent - USER_LOGOUT: " + user.getName());
			UserManager.saveUserToDB(user.getName());
			UserManager.removeUser(user.getName());
			break;
		case PUBLIC_MESSAGE:
			user = (User)event.getParameter(SFSEventParam.USER);
			String message = (String)event.getParameter(SFSEventParam.MESSAGE);
			ISFSObject sfsObjData = (ISFSObject)event.getParameter(SFSEventParam.OBJECT);
			try {
				jsonData = new JSONObject(Util.StringFromByteArray(sfsObjData.getByteArray("jsonData")));
				trace("GambleEventHandler PUBLIC_MESSAGE " + message + " from: " + user.toString() + " data:" + jsonData.toString());
			} catch (JSONException e) {
				trace("GambleEventHandler PUBLIC_MESSAGE JSONObject Error:" + e.toString());
			}

			break;
		case USER_LEAVE_ROOM:
			trace("##handleServerEvent - USER_LEAVE_ROOM: " + (String) event.getParameter(SFSEventParam.LOGIN_NAME));
			break;
		case ROOM_REMOVED:
			trace("##handleServerEvent - ROOM_REMOVED: " + (Room)event.getParameter(SFSEventParam.ROOM));
			break;
		case BUDDY_LIST_INIT:
			trace("BUDDY_LIST_INIT");
			user = (User)event.getParameter(SFSEventParam.USER);
			break;
		default:
			break;
		}
	}
	
	private void setUserVariables(User player, JSONObject jsonData) {
		try {
			trace("setUserVariables:"+ player + " | " + jsonData.toString());
			ArrayList<UserVariable> variables = new ArrayList<UserVariable>();
			SFSUserVariable variable = new SFSUserVariable("displayName", jsonData.getString("displayName"), false);
			variables.add(variable);
			variable = new SFSUserVariable("cash", jsonData.getInt("cash"), false);
			variables.add(variable);
			player.setVariables(variables);
			
			UserManager.setBuddyVariables(player, jsonData, true);
			
		} catch (Exception exception) {
			trace("setUserVariables:Exception:" + exception.toString());
		}
	}
	
//	private void setBuddyVariables(User player, JSONObject jsonData, boolean shouldInitBuddyList) {
//		try {
//			trace("setBuddyVariables:"+ player + " | " + jsonData.toString());
//			buddyApi = SmartFoxServer.getInstance().getAPIManager().getBuddyApi();
//			if (shouldInitBuddyList) {
//				buddyApi.initBuddyList(player, false);
//			}
//			List<BuddyVariable> vars = new ArrayList<BuddyVariable>();
//			vars.add( new SFSBuddyVariable("displayName", player.getVariable("displayName").getStringValue()));
//			vars.add( new SFSBuddyVariable("cash", player.getVariable("cash").getIntValue()));
//			vars.add( new SFSBuddyVariable("facebookId", jsonData.getString("facebookId")));
//			vars.add( new SFSBuddyVariable("avatar", jsonData.getString("avatar")));
//			vars.add( new SFSBuddyVariable("$cash", player.getVariable("cash").getIntValue()));
//			vars.add( new SFSBuddyVariable("$displayName", player.getVariable("displayName").getStringValue()));
//			vars.add( new SFSBuddyVariable("$facebookId", jsonData.getString("facebookId")));
//			vars.add( new SFSBuddyVariable("$avatar", jsonData.getString("avatar")));
//			buddyApi.setBuddyVariables(player, vars, true, false);
//		} catch (Exception exception) {
//			trace("setUserVariables:Exception:" + exception.toString());
//		}
//	}
	
//	private void resetUserVariables(User player) {
//		try {
//		} catch (Exception exception) {
//			trace("resetUserGlobalRoomName:Exception:" + exception.toString());
//		}
//	}
}