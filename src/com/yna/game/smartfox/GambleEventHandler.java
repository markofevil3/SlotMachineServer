package com.yna.game.smartfox;

import org.json.JSONException;
import org.json.JSONObject;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.exceptions.SFSErrorCode;
import com.smartfoxserver.v2.exceptions.SFSErrorData;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.exceptions.SFSLoginException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import com.smartfoxserver.bitswarm.sessions.Session;
import com.yna.game.common.ErrorCode;
import com.yna.game.common.Util;

public class GambleEventHandler extends BaseServerEventHandler {

	private static final int NEW_USER_CASH = 100000;
	
	@Override
	public void handleServerEvent(ISFSEvent event) throws SFSException {
		JSONObject jsonData;
		switch (event.getType()) {
		case USER_LOGIN:
			String username = (String) event.getParameter(SFSEventParam.LOGIN_NAME);
			String password = (String) event.getParameter(SFSEventParam.LOGIN_PASSWORD);
			ISFSObject outData = (ISFSObject)event.getParameter(SFSEventParam.LOGIN_OUT_DATA);
			ISFSObject sfsObj = (ISFSObject)event.getParameter(SFSEventParam.LOGIN_IN_DATA);
			int errorCode = 0;
			try {
				jsonData = new JSONObject(Util.StringFromByteArray(sfsObj.getByteArray("jsonData")));
				if (jsonData.getBoolean("isRegister")) {
					Boolean isGuest = jsonData.getBoolean("isGuest");
					trace("GambleEventHandler : USER_REGISTER " + username + " " + password + " guest:" + isGuest + "#");
					// Register new user
					UserManager.registerUser(username, jsonData);
					jsonData.put("cash", NEW_USER_CASH);
					outData.putByteArray("jsonData", Util.StringToBytesArray(jsonData.toString()));
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
			break;
		case USER_DISCONNECT:
				trace("##handleServerEvent - USER_DISCONNECT: " + (String) event.getParameter(SFSEventParam.LOGIN_NAME));
			break;
		case PUBLIC_MESSAGE:
			User user = (User)event.getParameter(SFSEventParam.USER);
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
			break;
		case ROOM_REMOVED:
			break;
		default:
			break;
		}
	}
}