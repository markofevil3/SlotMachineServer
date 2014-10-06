package com.yna.game.smartfox.handler;

import org.json.JSONObject;

import com.smartfoxserver.v2.annotations.MultiHandler;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.yna.game.common.ErrorCode;
import com.yna.game.common.Util;
import com.yna.game.smartfox.ClientRequestHandler;
import com.yna.game.tienlen.models.Command;

@MultiHandler
public class PokerHandler extends ClientRequestHandler {

	@Override
	protected void handleRequest(String commandId, User player, ISFSObject params, JSONObject out) {
		trace("PokerHandler handleRequest:" + commandId);
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
		case Command.LOBBY:
//			handleLobbyCommand(player, jsonData, out);
		break;
		case Command.CREATE:
//			handleCreateCommand(player, jsonData, out);
			break;
		case Command.START:
//			handleStartCommand(player, jsonData, out);
			break;
		case Command.JOIN:
//			handleJoinCommand(player, jsonData, out);
			break;
		case Command.KICK:
//			handleKickCommand(player, jsonData, out);
			break;
		case Command.DROP:
//			handleDropCommand(player, jsonData, out);
			break;
		case Command.FOLD:
//			handleFoldCommand(player, jsonData, out);
			break;
		case Command.SIT:
//			handleSitCommand(player, jsonData, out);
			break;
		case Command.STANDUP:
//			handleStandupCommand(player, jsonData, out);
			break;
		case Command.LEAVE:
//			handleLeaveCommand(player, jsonData, out);
			break;
		case Command.QUIT:
//			handleQuitCommand(player, jsonData, out);
			break;
		}
	}
}
