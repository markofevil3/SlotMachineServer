package com.yna.game.smartfox.handler;

import java.util.ArrayList;

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
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.yna.game.common.ErrorCode;
import com.yna.game.common.Util;
import com.yna.game.smartfox.ClientRequestHandler;
import com.yna.game.smartfox.GameId;
import com.yna.game.tienlen.models.Command;
import com.yna.game.tienlen.models.GameConfig;
import com.yna.game.tienlen.models.GameManager;
import com.yna.game.tienlen.models.GameRoom;

@MultiHandler
public class TienLenMienBacHandler extends ClientRequestHandler {
	public class PublishCommand {
		public static final int NULL = -1;
		public static final int START_GAME = 0;
		public static final int UPDATE_GAME = 1;
	}
	
	private int publishCommand = PublishCommand.NULL;
	private GameRoom gameRoom = null;
	private User exceptUser = null;
	private JSONObject jsonData = null;
	
	@Override
	protected void handleRequest(String commandId, User player, ISFSObject params, JSONObject out) {
		trace("handleRequest:" + commandId);
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
			handleLobbyCommand(player, jsonData, out);
		break;
		case Command.CREATE:
			handleCreateCommand(player, jsonData, out);
			break;
		case Command.START:
			handleStartCommand(player, jsonData, out);
			break;
		case Command.JOIN:
			handleJoinCommand(player, jsonData, out);
			break;
		case Command.KICK:
			handleKickCommand(player, jsonData, out);
			break;
		case Command.DROP:
			handleDropCommand(player, jsonData, out);
			break;
		case Command.FOLD:
			handleFoldCommand(player, jsonData, out);
			break;
		case Command.SIT:
			handleSitCommand(player, jsonData, out);
			break;
		case Command.STANDUP:
			handleStandupCommand(player, jsonData, out);
			break;
		case Command.LEAVE:
			handleLeaveCommand(player, jsonData, out);
			break;
		case Command.QUIT:
			handleQuitCommand(player, jsonData, out);
			break;
		}
	}
	
	@Override
	protected void handleAfterRequest() {
		trace("handleAfterRequest");
		switch (publishCommand) {
		case PublishCommand.START_GAME:
			GameManager.publishStartGame(gameRoom, exceptUser);
			break;
		case PublishCommand.UPDATE_GAME:
			GameManager.publishGame(gameRoom, exceptUser, jsonData);
			break;
		}
		trace("handleAfterRequest:done");
	}
	
	public void prepareModerateMessage(int publishCommand, GameRoom gameRoom, User exceptUser, JSONObject jsonData) {
		this.publishCommand = publishCommand;
		this.gameRoom = gameRoom;
		this.exceptUser = exceptUser;
		this.jsonData = jsonData;
	}
	
	private void handleLobbyCommand(User player, JSONObject jsonData, JSONObject out) {
		try {
			GameManager.getAll(player, out);
		} catch (Exception exception) {
			trace("handleLobbyCommand:Exception:" + exception.toString());
		}
	}
	
	private void handleCreateCommand(User player, JSONObject jsonData, JSONObject out) {
		try {
			GameConfig gameConfig = new GameConfig(jsonData.getJSONObject("gameConfig"));
			
			if (!gameConfig.isValid()) {
				out.put(ErrorCode.PARAM, ErrorCode.Tienlen.INVALID_GAMECONFIG);
				return;
			}
			
			String userId = player.getName();
			int seatIndex = jsonData.getInt("seatIndex");
			
			// TODO: Should check user here.
			int userError = ErrorCode.Tienlen.NULL;
			out.put(ErrorCode.PARAM, userError);
			
			if (userError == ErrorCode.Tienlen.NULL) {
				GameRoom gameRoom = GameManager.createRoom(this, player, gameConfig, userId, seatIndex);
				
				if (gameRoom == null) {
					out.put(ErrorCode.PARAM, ErrorCode.Tienlen.CANNOT_CREATE_ROOM);
					return;
				}
				
				int createRoomError = createSFSRoom(gameRoom.roomId, player, userId);
				out.put(ErrorCode.PARAM, createRoomError);
				out.put("gameRoom", gameRoom.toJson());

				if (createRoomError != ErrorCode.Tienlen.NULL) {
					GameManager.removeRoom(gameRoom.roomId);
				} else {
					setUserVariables(player, gameRoom.roomId);
				}
			}
		} catch (Exception exception) {
			try {
				out.put(ErrorCode.PARAM, ErrorCode.Tienlen.UNKNOWN);
			} catch (Exception ex) {
				
			}
			
			trace("handleCreateCommand:Exception:" + exception.toString());
		}
	}
	
	private void handleStartCommand(User player, JSONObject jsonData, JSONObject out) {
		try {
			String roomId = getPlayerRoomId(player);
			trace("handleStartCommand:roomId:" + roomId);
			
			if (roomId == null) {
				out.put(ErrorCode.PARAM, ErrorCode.Tienlen.GAME_NOT_EXISTS);
				return;
			}
			
			GameManager.start(this, player, roomId, player.getName(), out);
		} catch (Exception exception) {
			trace("handleStartCommand:Exception:" + exception.toString());
		}
	}
	
	private void handleJoinCommand(User player, JSONObject jsonData, JSONObject out) {
		try {
			String roomId = jsonData.getString("roomId");
			trace("handleJoinCommand:roomId:" + roomId);
			
			if (roomId == null) {
				out.put(ErrorCode.PARAM, ErrorCode.Tienlen.GAME_NOT_EXISTS);
				return;
			}
			
			GameManager.joinUser(this, player, roomId, player.getName(), out);
			int errorCode = out.getInt(ErrorCode.PARAM);
			out.put(ErrorCode.PARAM, errorCode);
			
			if (errorCode == ErrorCode.Tienlen.NULL) {
				try {
					Room createdRoom = zone.getRoomByName(roomId);
					
					if (createdRoom != null) {
						sfsApi.joinRoom(player, createdRoom, null, false, null, true, true);
						setUserVariables(player, roomId);
					} else {
						out.put(ErrorCode.PARAM, ErrorCode.Tienlen.GAME_NOT_EXISTS);
					}
				} catch (Exception exception) {
					trace("GetSFSRoom:Exception:" + exception.toString());
					out.put(ErrorCode.PARAM, ErrorCode.Tienlen.UNKNOWN);
				}
			}
		} catch (Exception exception) {
			trace("handleJoinCommand:Exception:" + exception.toString());
		}
	}
	
	private void handleLeaveCommand(User player, JSONObject jsonData, JSONObject out) {
		try {
			String roomId = getPlayerRoomId(player);
			trace("handleLeaveCommand:roomId:" + roomId);
			
			if (roomId == null) {
				out.put(ErrorCode.PARAM, ErrorCode.Tienlen.GAME_NOT_EXISTS);
				return;
			}
			
			GameManager.leave(this, player, roomId, player.getName(), out);
			
			if (out.getInt(ErrorCode.PARAM) == ErrorCode.Tienlen.NULL) {
				removeUserOutOfSFSRoom(roomId, player);
				resetUserVariables(player);
			}
		} catch (Exception exception) {
			trace("handleLeaveCommand:Exception:" + exception.toString());
		}	
	}
	
	private void handleDropCommand(User player, JSONObject jsonData, JSONObject out) {
		try {
			String roomId = getPlayerRoomId(player);
			trace("handleDropCommand:roomId:" + roomId);
			
			if (roomId == null) {
				out.put(ErrorCode.PARAM, ErrorCode.Tienlen.GAME_NOT_EXISTS);
				return;
			}
			
			GameManager.drop(this, player, roomId, player.getName(), jsonData.getString("cardsString"), out);
		} catch (Exception exception) {
			trace("handleDropCommand:Exception:" + exception.toString());
		}
	}
	
	private void handleFoldCommand(User player, JSONObject jsonData, JSONObject out) {
		try {
			String roomId = getPlayerRoomId(player);
			trace("handleFoldCommand:roomId:" + roomId);
			
			if (roomId == null) {
				out.put(ErrorCode.PARAM, ErrorCode.Tienlen.GAME_NOT_EXISTS);
				return;
			}
			
			GameManager.fold(this, player, roomId, player.getName(), out);
		} catch (Exception exception) {
			trace("handleFoldCommand:Exception:" + exception.toString());
		}
	}
	
	private void handleSitCommand(User player, JSONObject jsonData, JSONObject out) {
		try {
			String roomId = getPlayerRoomId(player);
			trace("handleSitCommand:roomId:" + roomId);
			
			if (roomId == null) {
				out.put(ErrorCode.PARAM, ErrorCode.Tienlen.GAME_NOT_EXISTS);
				return;
			}
			
			GameManager.sit(this, player, roomId, player.getName(), jsonData.getInt("seatIndex"), out);
		} catch (Exception exception) {
			trace("handleSitCommand:Exception:" + exception.toString());
		}
	}
	
	private void handleStandupCommand(User player, JSONObject jsonData, JSONObject out) {
		try {
			String roomId = jsonData.getString("roomId");
			trace("handleStandupCommand:roomId:" + roomId);
			
			if (roomId == null) {
				out.put(ErrorCode.PARAM, ErrorCode.Tienlen.GAME_NOT_EXISTS);
				return;
			}
			
			GameManager.standup(this, player, roomId, player.getName(), out);
		} catch (Exception exception) {
			trace("handleStandupCommand:Exception:" + exception.toString());
		}
	}
	
	private void handleKickCommand(User player, JSONObject jsonData, JSONObject out) {
	}
	
	private void handleQuitCommand(User player, JSONObject jsonData, JSONObject out) {
		try {
			String roomId = jsonData.getString("roomId");
			trace("handleQuitCommand:roomId:" + roomId);
			
			if (roomId == null) {
				out.put(ErrorCode.PARAM, ErrorCode.Tienlen.GAME_NOT_EXISTS);
				return;
			}
			
			GameManager.quitUser(this, player, roomId, player.getName(), out);
			int errorCode = out.getInt(ErrorCode.PARAM);
			
			if (errorCode == ErrorCode.Tienlen.NULL) {
				removeUserOutOfSFSRoom(roomId, player);
				resetUserVariables(player);
			}
		} catch (Exception exception) {
			trace("handleQuitCommand:Exception:" + exception.toString());
		}
	}
	
	private void removeUserOutOfSFSRoom(String roomId, User player) {
		try {
			Room joinedRoom = zone.getRoomByName(roomId);
			
			if (joinedRoom != null) {
				sfsApi.leaveRoom(player, joinedRoom, true, false);
			}
		} catch (Exception exception) {
			trace("removeUserOutOfSFSRoom:Exception:" + exception.toString());
		}
	}
	
	private int createSFSRoom(String roomId, User player, String userId) {
		int errorCode = ErrorCode.Tienlen.NULL;
		CreateRoomSettings roomSettings = new CreateRoomSettings();
		roomSettings.setMaxUsers(GameRoom.MAX_USERS);
		roomSettings.setGroupId(GameId.TLMB);
		roomSettings.setName(roomId);
		roomSettings.setAutoRemoveMode(SFSRoomRemoveMode.WHEN_EMPTY);

		try {
			Room createdRoom = sfsApi.createRoom(zone, roomSettings, null, true, null);
			sfsApi.joinRoom(player, createdRoom, null, false, null, true, true);
		} catch (Exception exception) {
			trace("CreateSFSRoom:Exception:" + exception.toString());
			errorCode = ErrorCode.Tienlen.UNKNOWN;
		}
		
		return errorCode;
	}
	
	private void setUserVariables(User player, String roomId) {
		try {
			ArrayList<UserVariable> variables = new ArrayList<UserVariable>();
			SFSUserVariable variable = new SFSUserVariable("roomId", roomId, true);
			variables.add(variable);
			variable = new SFSUserVariable("gameId", GameId.TLMB, true);
			variables.add(variable);
			player.setVariables(variables);
		} catch (Exception exception) {
			trace("setUserGlobalRoomName:Exception:" + exception.toString());
		}
	}
	
	private void resetUserVariables(User player) {
		try {
			player.removeVariable("roomId");
			player.removeVariable("gameId");
		} catch (Exception exception) {
			trace("resetUserGlobalRoomName:Exception:" + exception.toString());
		}
	}
	
	private String getPlayerRoomId(User player) {
		try {
			SFSUserVariable variable = (SFSUserVariable)player.getVariable("roomId");

			if (variable != null) {
				return variable.getStringValue();
			}
		} catch (Exception exception) {
			trace("GetUserGlobalRoomName:Exception:" + exception.toString());
		}

		return null;
	}
}