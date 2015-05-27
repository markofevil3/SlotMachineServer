package com.yna.game.smartfox.handler;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.annotations.MultiHandler;
import com.smartfoxserver.v2.api.ISFSBuddyApi;
import com.smartfoxserver.v2.api.ISFSGameApi;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.invitation.Invitation;
import com.smartfoxserver.v2.entities.invitation.InvitationCallback;
import com.yna.game.common.ErrorCode;
import com.yna.game.common.GameConstants;
import com.yna.game.common.Util;
import com.yna.game.smartfox.ClientRequestHandler;
import com.yna.game.smartfox.UserManager;
import com.yna.game.tienlen.models.Command;

@MultiHandler
public class UserRequestHandler extends ClientRequestHandler {
	
	private static ISFSGameApi gameAPI;
	
	public static void init() {
		gameAPI = SmartFoxServer.getInstance().getAPIManager().getGameApi();
	}	
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
			handleLoadLeaderboardCommand(player, jsonData, out);
			break;
		case Command.LOAD_USER_INFO:
			handleLoadUserInfoCommand(player, jsonData, out);
			break;
		case Command.LOAD_INBOX:
			handleLoadInbox(player, jsonData, out);
			break;
		case Command.ADD_FRIEND:
			handleAddFriendCommand(player, jsonData, out);
			break;
		case Command.ADD_FB_FRIEND:
			handleAddFbFriendCommand(player, jsonData, out);
			break;
		case Command.INVITE_TO_GAME:
			handleCommandInviteToGame(player, jsonData, out);
			break;
		case Command.CLAIM_DAILY:
			handleCommandClaimDaily(player, jsonData, out);
			break;
		}
	}
	
	private void handleCommandClaimDaily(User player, JSONObject jsonData, JSONObject out) {
		try {
			out = UserManager.claimDailyReward(jsonData.getString("username"), out);
		} catch (Exception exception) {
			trace("handleCommandClaimDaily:JSONObject Exception:" + exception.toString());
		}
	}	
	
	private void handleLoadLeaderboardCommand(User player, JSONObject jsonData, JSONObject out) {
		try {
			// TO DO: Get leaderboard by type
			out.put("users", UserManager.getLeaderboardUsers());
			out.put("type", jsonData.getInt("type"));
		} catch (Exception exception) {
			trace("handleLoadLeaderboardCommand:JSONObject Exception:" + exception.toString());
		}
	}
		
	private void handleLoadInbox(User player, JSONObject jsonData, JSONObject out) {
		try {
			// TO DO: load user inbox messages
			out.put("inbox", UserManager.getUserInbox(jsonData.getString("username")));
		} catch (Exception exception) {
			trace("handleLoadInbox:JSONObject Exception:" + exception.toString());
		}
	}
	
	private void handleLoadUserInfoCommand(User player, JSONObject jsonData, JSONObject out) {
		try {
			out.put("user", UserManager.getUser(jsonData.getString("username"), true));
		} catch (Exception exception) {
			trace("handleLoadUserInfoCommand:JSONObject Exception:" + exception.toString());
		}
	}
	
	private void handleAddFriendCommand(User player, JSONObject jsonData, JSONObject out) {
		try {
			ISFSBuddyApi buddyApi = SmartFoxServer.getInstance().getAPIManager().getBuddyApi();
			trace(buddyApi + " " + jsonData.getString("fUsername") + " " + player);
			buddyApi.addBuddy(player, jsonData.getString("fUsername"), false, true, false);
			out.put(ErrorCode.PARAM, ErrorCode.User.NULL);
			out.put("fUsername", jsonData.getString("fUsername"));
		} catch (Exception exception) {
			trace("handleAddFriendCommand:JSONObject Exception:" + exception.toString());
		}
	}
	
	private void handleAddFbFriendCommand(User player, JSONObject jsonData, JSONObject out) {
		try {
			JSONArray usernameArr = UserManager.GetUsernamesByFbIds(jsonData.getJSONArray("fbIds"));
			ISFSBuddyApi buddyApi = SmartFoxServer.getInstance().getAPIManager().getBuddyApi();
			for (int i = 0; i < usernameArr.length(); i++) {
				buddyApi.addBuddy(player, usernameArr.getString(i), false, true, false);
			}
			out.put(ErrorCode.PARAM, ErrorCode.User.NULL);
		} catch (Exception exception) {
			trace("handleAddFriendCommand:JSONObject Exception:" + exception.toString());
		}
	}
	
	private void handleCommandInviteToGame(User player, JSONObject jsonData, JSONObject out) {
		try {
			JSONArray inviteeNames = jsonData.getJSONArray("invitees");
			List<User> invitees = new ArrayList<User>();
			for (int i = 0; i < inviteeNames.length(); i++) {
				invitees.add(zone.getUserByName(inviteeNames.getString(i)));
			}
			ISFSObject params = new SFSObject();
			params.putUtfString("gameType", jsonData.getString("gameType"));
			params.putUtfString("message", jsonData.getString("message"));
			params.putUtfString("roomName", jsonData.getString("roomName"));

	    gameAPI.sendInvitation(player, invitees, GameConstants.INVITE_MESSAGE_EXPIRED_SECONDS, new InvitationCallback() {
        @Override
        public void onRefused(Invitation invObj, ISFSObject params)
        {
            // Handle the refused invitation
        }
         
        @Override
        public void onExpired(Invitation invObj)
        {
            // Handle the expired invitation
        }
         
        @Override
        public void onAccepted(Invitation invObj, ISFSObject params)
        {
            // Handle the accepted invitation
        }
	    }, params); 
		} catch (JSONException e) {
			trace("handleCommandInviteToGame Exception:" + e.toString());
		}
	}
}
