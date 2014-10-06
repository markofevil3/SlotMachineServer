package com.yna.game.smartfox.handler;

import org.json.JSONObject;

import com.smartfoxserver.v2.annotations.MultiHandler;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.yna.game.common.ErrorCode;
import com.yna.game.common.Util;
import com.yna.game.smartfox.ClientRequestHandler;
import com.yna.game.smartfox.UserManager;
import com.yna.game.tienlen.models.Command;

@MultiHandler
public class UserRequestHandler extends ClientRequestHandler {
	@Override
	protected void handleRequest(String commandId, User player, ISFSObject params, JSONObject out) {
		trace("UserRequestHandler handleRequest:" + commandId);
		JSONObject jsonData = null;
		
		try {
			jsonData = new JSONObject(Util.StringFromByteArray(params.getByteArray("jsonData")));
		} catch (Exception exception) {
			trace("handleRequest:GetJsonData:Exception:" + exception.toString());
			jsonData = null;
		}
		
		if (jsonData == null) {
			try {
				out.put(ErrorCode.PARAM, ErrorCode.Tienlen.UNKNOWN);
			} catch (Exception exception) {
				trace("handleRequest:PutJsonData:Exception:" + exception.toString());
			}
			return;
		}
		
		switch (commandId) {
		case Command.LOAD_LEADERBOARD:
			handleLoadLeaderboardCommand(jsonData, out);
			break;
		case Command.LOAD_FRIEND_LIST:
			handleLoadFriendListCommand(jsonData, out);
			break;
		case Command.LOAD_USER_INFO:
			handleLoadUserInfoCommand(jsonData, out);
			break;
		case Command.ADD_FRIEND:
			handleAddFriendCommand(jsonData, out);
			break;
		}
	}
	
	private void handleLoadLeaderboardCommand(JSONObject jsonData, JSONObject out) {
		try {
			// TO DO: Get top user by type - should cache
			out.put("users", UserManager.getAllUsers());
			out.put("type", jsonData.getInt("type"));
		} catch (Exception exception) {
			trace("handleLoadLeaderboardCommand:JSONObject Exception:" + exception.toString());
		}
	}
	
	private void handleLoadFriendListCommand(JSONObject jsonData, JSONObject out) {
		try {
			out.put("friends", UserManager.getUsers(jsonData.getJSONArray("friends")));
		} catch (Exception exception) {
			trace("handleLoadFriendListCommand Exception:" + exception.toString());
		}
	}
	
	private void handleLoadUserInfoCommand(JSONObject jsonData, JSONObject out) {
		try {
			out.put("user", UserManager.getUser(jsonData.getString("username")));
		} catch (Exception exception) {
			trace("handleLoadUserInfoCommand:JSONObject Exception:" + exception.toString());
		}
	}
	
	private void handleAddFriendCommand(JSONObject jsonData, JSONObject out) {
		try {
			UserManager.addFriend(jsonData.getString("username"), jsonData.getString("fUsername"), out);
		} catch (Exception exception) {
			trace("handleLoadUserInfoCommand:JSONObject Exception:" + exception.toString());
		}
	}
}
