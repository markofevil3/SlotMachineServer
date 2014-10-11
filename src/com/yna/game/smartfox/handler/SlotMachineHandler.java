package com.yna.game.smartfox.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.smartfoxserver.bitswarm.sessions.ISession;
import com.smartfoxserver.v2.annotations.MultiHandler;
import com.smartfoxserver.v2.api.CreateRoomSettings;
import com.smartfoxserver.v2.api.SFSApi;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.SFSRoomRemoveMode;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.Zone;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.match.BoolMatch;
import com.smartfoxserver.v2.entities.match.MatchExpression;
import com.smartfoxserver.v2.entities.match.RoomProperties;
import com.smartfoxserver.v2.entities.match.StringMatch;
import com.smartfoxserver.v2.entities.variables.RoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSRoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.exceptions.SFSVariableException;
import com.yna.game.common.ErrorCode;
import com.yna.game.common.Util;
import com.yna.game.smartfox.ClientRequestHandler;
import com.yna.game.smartfox.GameId;
import com.yna.game.slotmachine.models.Command;
import com.yna.game.slotmachine.models.GameType;
import com.yna.game.slotmachine.models.SlotCombinations;

@MultiHandler
public class SlotMachineHandler extends ClientRequestHandler {
	
	private final int ROOM_NAME_LENGTH = 5;
	private final int MAX_USERS_PER_ROOM = 5;
	
	private User exceptUser = null;
	private JSONObject jsonData = null;
	
	@Override
	protected void handleRequest(String commandId, User player, ISFSObject params, JSONObject out) {
		trace("slot machine handleRequest:" + commandId);
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
		case Command.SLOT_JOIN_ROOM:
			handleJoinRoomCommand(player, jsonData, out);
		break;
		case Command.SLOT_PLAY:
			handlePlayCommand(player, jsonData, out);
		break;
		case Command.SLOT_LEAVE:
			handleLeaveCommand(player, jsonData, out);
		break;
//		case Command.CREATE:
//			handleCreateCommand(player, jsonData, out);
//			break;
//		case Command.START:
//			handleStartCommand(player, jsonData, out);
//			break;
//		case Command.JOIN:
//			handleJoinCommand(player, jsonData, out);
//			break;
//		case Command.KICK:
//			handleKickCommand(player, jsonData, out);
//			break;
//		case Command.DROP:
//			handleDropCommand(player, jsonData, out);
//			break;
//		case Command.FOLD:
//			handleFoldCommand(player, jsonData, out);
//			break;
//		case Command.SIT:
//			handleSitCommand(player, jsonData, out);
//			break;
//		case Command.STANDUP:
//			handleStandupCommand(player, jsonData, out);
//			break;
//		case Command.LEAVE:
//			handleLeaveCommand(player, jsonData, out);
//			break;
//		case Command.QUIT:
//			handleQuitCommand(player, jsonData, out);
//			break;
		}
	}
	
	private void handleJoinRoomCommand(User player, JSONObject jsonData, JSONObject out) {
		try {
			String gameType = jsonData.getString("gameType");
			// Join user to lobby
			Room lobbyRoom = zone.getRoomByName(GameType.GetLoobyRoom(gameType));

			sfsApi.joinRoom(player, lobbyRoom, null, false, null, true, true);
			// Find available game room
		// Prepare match expression
			MatchExpression exp = new MatchExpression(RoomProperties.IS_GAME, BoolMatch.EQUALS, true).and
			                    (RoomProperties.HAS_FREE_PLAYER_SLOTS, BoolMatch.EQUALS, true);
			  
			// Search Rooms
			List<Room> joinableRooms = sfsApi.findRooms(zone.getRoomListFromGroup(GameType.GetRoomGroup(gameType)), exp, 1);
			if (joinableRooms.size() > 0) {
				sfsApi.joinRoom(player, joinableRooms.get(0), null, false, null, true, true);
				player.setVariable(new SFSUserVariable("gRoomId", joinableRooms.get(0).getName(), true));
				out.put(ErrorCode.PARAM, ErrorCode.SlotMachine.NULL);
			} else {
				out.put(ErrorCode.PARAM, createSFSGameRoom(player, gameType));
			}
			out.put("gameType", gameType);
		} catch (Exception exception) {
			trace("handleJoinRoomCommand:" + exception.toString());
		}
	}
	
	private int createSFSGameRoom(User player, String gameType) {
		int errorCode = ErrorCode.SlotMachine.NULL;
		String roomName = GameType.GetRoomCode(gameType) + Util.generateRandomString(ROOM_NAME_LENGTH);

		while (zone.getRoomByName(roomName) != null) {
			roomName = GameType.GetRoomCode(gameType) + Util.generateRandomString(ROOM_NAME_LENGTH);
		}
		CreateRoomSettings roomSettings = new CreateRoomSettings();
		roomSettings.setMaxUsers(MAX_USERS_PER_ROOM);
		roomSettings.setGroupId(GameType.GetRoomGroup(gameType));
		roomSettings.setName(roomName);
		roomSettings.setAutoRemoveMode(SFSRoomRemoveMode.WHEN_EMPTY);
		try {
			player.setVariable(new SFSUserVariable("gRoomId", roomName, true));
		} catch (SFSVariableException e) {
			trace("CreateSFSRoom:SFSVariableException:" + e.toString());
		}
		try {
			Room createdRoom = sfsApi.createRoom(zone, roomSettings, null, true, null);
			sfsApi.joinRoom(player, createdRoom, null, false, null, true, true);
		} catch (Exception exception) {
			trace("CreateSFSRoom:Exception:" + exception.toString());
			errorCode = ErrorCode.Tienlen.UNKNOWN;
		}
		trace("createSFSGameRoom");
		return errorCode;
	}
	
	private void handlePlayCommand(User player, JSONObject jsonData, JSONObject out) {
		try {
			int betPerLine = jsonData.getInt("betPerLine");
			int numLines = jsonData.getInt("numLines");
			int totalCost = betPerLine * numLines;
			String gameType = jsonData.getString("gameType");
			Room lobbyRoom = zone.getRoomByName(GameType.GetLoobyRoom(gameType));
			trace("handlePlayCommand " + lobbyRoom.getVariable("jackpot"));
			RoomVariable jackpot = new SFSRoomVariable("jackpot", lobbyRoom.getVariable("jackpot").getIntValue() + totalCost);
			sfsApi.setRoomVariables(null, lobbyRoom, Arrays.asList(jackpot));
			try {
				lobbyRoom.setVariable(jackpot);
			} catch (SFSVariableException e) {
				trace("handlePlayCommand setVariable:" + e.toString());
			}
			int[] randomItems = SlotCombinations.GenerateRandomItems();
			out.put("items", randomItems);
			out.put("winResults", SlotCombinations.CalculateCombination(randomItems, numLines, betPerLine));
		} catch (JSONException e) {
			trace("handlePlayCommand:" + e.toString());
		}
	}
	
	private void handleLeaveCommand(User player, JSONObject jsonData, JSONObject out) {
		try {
			String gameType = jsonData.getString("gameType");
			Room lobbyRoom = zone.getRoomByName(GameType.GetLoobyRoom(gameType));
			sfsApi.leaveRoom(player, lobbyRoom, false, false);
			Room gameRoom = zone.getRoomByName(player.getVariable("gRoomId").getStringValue());
			sfsApi.leaveRoom(player, gameRoom, true, false);
		} catch (Exception exception) {
			trace("removeUserOutOfSFSRoom:Exception:" + exception.toString());
		}
	}
}
