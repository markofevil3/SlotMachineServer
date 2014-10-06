package com.yna.game.tienlen.models;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONObject;

import com.smartfoxserver.bitswarm.sessions.ISession;
import com.smartfoxserver.v2.api.SFSApi;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.Zone;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.SFSExtension;
import com.yna.game.common.ErrorCode;
import com.yna.game.common.Util;
import com.yna.game.smartfox.GameId;
import com.yna.game.smartfox.handler.TienLenMienBacHandler;

public class GameManager {
	public static final int MAX_ROOMS = 500;
	public static final int ROOM_NAME = 10;
	
	public static Hashtable<String, GameRoom> rooms;
	public static SFSExtension gameExtension;
	
	public static void init(SFSExtension extension) {
		rooms = new Hashtable<String, GameRoom>();
		gameExtension = extension;
	}
	
	public static void destroy() {
		Enumeration<GameRoom> games = rooms.elements();
		
		while (games.hasMoreElements()) {
			games.nextElement().destroy();
		}
		
		rooms.clear();
	}
	
	public static void getAll(User player, JSONObject outData) {
		try {
			JSONArray arr = new JSONArray();
			Enumeration<GameRoom> games = rooms.elements();
			
			while (games.hasMoreElements()) {
				arr.put(games.nextElement().toLobbyJson());
			}
			
			outData.put("rooms", arr);
		} catch (Exception exception) {
			Util.log("GameManager:getAll:Exception:" + exception.toString());
		}
	}
	
	public static GameRoom createRoom(TienLenMienBacHandler handler, User player, GameConfig gameConfig, String userId, int seatIndex) {
		synchronized (rooms) {
			String roomId = Util.generateRandomString(ROOM_NAME);
			
			while (rooms.containsKey(roomId)) {
				roomId = Util.generateRandomString(ROOM_NAME);
			}
			
			GameRoom newRoom = new GameRoom(roomId, gameConfig);
			newRoom.initUser(userId, seatIndex, true);
			rooms.put(roomId, newRoom);
			GameRoom gameRoom = (GameRoom)rooms.get(roomId);
			Util.log("add new room:" + gameRoom);
			return newRoom;
		}
	}
	
	public static void joinUser(TienLenMienBacHandler handler, User player, String roomId, String userId, JSONObject outData) {
		try {
			int errorCode = ErrorCode.Tienlen.NULL;
			GameRoom gameRoom = (GameRoom)rooms.get(roomId);
			Util.log("join " + roomId + " " + gameRoom);
			
			if (gameRoom != null) {
				gameRoom.print();
				outData.put("gameRoom", gameRoom.toJson());
			} else {
				errorCode = ErrorCode.Tienlen.GAME_NOT_EXISTS;
			}
			
			outData.put(ErrorCode.PARAM, errorCode);
		} catch (Exception exception) {
			Util.log("GameManager:joinUser:Exception:" + exception.toString());
		}
	}
	
	public static void leave(TienLenMienBacHandler handler, User player, String roomId, String userId, JSONObject outData) {
		try {
			int errorCode = ErrorCode.Tienlen.NULL;
			GameRoom gameRoom = (GameRoom)rooms.get(roomId);
			Util.log("leave " + roomId + " " + gameRoom);
			
			if (gameRoom != null) {
				gameRoom.standup(userId);
				gameRoom.print();
				
				if (gameRoom.isEmpty()) {
					removeRoom(roomId);
				} else {
					JSONObject updateJsonData = new JSONObject();
					updateJsonData.put("updateDataType", UpdateData.USER_LEAVE);
					updateJsonData.put("userLeave", gameRoom.getUserGameByUserId(userId).forOtherToJson());
					outData.put("updateData", updateJsonData);
					handler.prepareModerateMessage(TienLenMienBacHandler.PublishCommand.UPDATE_GAME, gameRoom, player, updateJsonData);
//					publishGame(gameRoom, player, outData);
				}
			} else {
				errorCode = ErrorCode.Tienlen.GAME_NOT_EXISTS;
			}
			
			outData.put(ErrorCode.PARAM, errorCode);
		} catch (Exception exception) {
			Util.log("GameManager:leave:Exception:" + exception.toString());
		}
	}
	
	public static void sit(TienLenMienBacHandler handler, User player, String roomId, String userId, int seatIndex, JSONObject outData) {
		try {
			int errorCode = ErrorCode.Tienlen.NULL;
			GameRoom gameRoom = (GameRoom)rooms.get(roomId);
			Util.log("sit " + roomId + " " + gameRoom);
			
			if (gameRoom != null) {
				gameRoom.initUser(userId, seatIndex, false);
				gameRoom.print();
				outData.put("gameRoom", gameRoom.toJson());
				
				JSONObject updateJsonData = new JSONObject();
				updateJsonData.put("updateDataType", UpdateData.USER_SIT);
				updateJsonData.put("userSit", gameRoom.getUserGameByUserId(userId).toJson());
				outData.put("updateData", updateJsonData);
				handler.prepareModerateMessage(TienLenMienBacHandler.PublishCommand.UPDATE_GAME, gameRoom, player, updateJsonData);
//				publishGame(gameRoom, player, outData);
			} else {
				errorCode = ErrorCode.Tienlen.GAME_NOT_EXISTS;
			}
		
			outData.put(ErrorCode.PARAM, errorCode);
		} catch (Exception exception) {
			Util.log("GameManager:sit:Exception:" + exception.toString());
		}
	}
	
	public static void kickUser(TienLenMienBacHandler handler, User player, String roomId, String userId, JSONObject outData) {
		
	}
	
	public static void standup(TienLenMienBacHandler handler, User player, String roomId, String userId, JSONObject outData) {
		try {
			int errorCode = ErrorCode.Tienlen.NULL;
			GameRoom gameRoom = (GameRoom)rooms.get(roomId);
			Util.log("standup " + roomId + " " + gameRoom);
			
			if (gameRoom != null) {
				gameRoom.standup(userId);
				gameRoom.print();
				
				JSONObject updateJsonData = new JSONObject();
				updateJsonData.put("updateDataType", UpdateData.USER_STANDUP);
				updateJsonData.put("userStandup", gameRoom.getUserGameByUserId(userId).forOtherToJson());
				outData.put("updateData", updateJsonData);
				handler.prepareModerateMessage(TienLenMienBacHandler.PublishCommand.UPDATE_GAME, gameRoom, player, updateJsonData);
//				publishGame(gameRoom, player, outData);
			} else {
				errorCode = ErrorCode.Tienlen.GAME_NOT_EXISTS;
			}
			
			outData.put(ErrorCode.PARAM, errorCode);
		} catch (Exception exception) {
			Util.log("GameManager:standup:Exception:" + exception.toString());
		}
	}
	
	public static void quitUser(TienLenMienBacHandler handler, User player, String roomId, String userId, JSONObject outData) {
		try {
			int errorCode = ErrorCode.Tienlen.NULL;
			GameRoom gameRoom = (GameRoom)rooms.get(roomId);
			Util.log("quit " + roomId + " " + gameRoom);
			
			if (gameRoom != null) {
				gameRoom.standup(userId);
				gameRoom.print();
				
				if (gameRoom.isEmpty()) {
					removeRoom(roomId);
				} else {
					JSONObject updateJsonData = new JSONObject();
					updateJsonData.put("updateDataType", UpdateData.USER_LEAVE);
					updateJsonData.put("userLeave", gameRoom.getUserGameByUserId(userId).toJson());
					outData.put("updateData", updateJsonData);
					handler.prepareModerateMessage(TienLenMienBacHandler.PublishCommand.UPDATE_GAME, gameRoom, player, updateJsonData);
//					publishGame(gameRoom, player, outData);
				}
			} else {
				errorCode = ErrorCode.Tienlen.GAME_NOT_EXISTS;
			}
			
			outData.put(ErrorCode.PARAM, errorCode);
		} catch (Exception exception) {
			Util.log("GameManager:quit:Exception:" + exception.toString());
		}
	}
	
	public static void start(TienLenMienBacHandler handler, User player, String roomId, String userId, JSONObject outData) {
		try {
			int errorCode = ErrorCode.Tienlen.NULL;
			GameRoom gameRoom = (GameRoom)rooms.get(roomId);
			Util.log("start " + roomId + " " + gameRoom);
			
			if (gameRoom != null) {
				gameRoom.start();
				gameRoom.print();
				handler.prepareModerateMessage(TienLenMienBacHandler.PublishCommand.START_GAME, gameRoom, null, null);
//				publishStartGame(gameRoom, null);
			}
			
			outData.put(ErrorCode.PARAM, errorCode);
		} catch (Exception exception) {
			Util.log("GameManager:start:Exception:" + exception.toString());
		}
	}
	
	public static void drop(TienLenMienBacHandler handler, User player, String roomId, String userId, String cardsString, JSONObject outData) {
		try {
			int errorCode = ErrorCode.Tienlen.NULL;
			GameRoom gameRoom = (GameRoom)rooms.get(roomId);
			Util.log("drop " + roomId + " " + gameRoom);
			
			if (gameRoom != null) {
				errorCode = gameRoom.drop(userId, cardsString);
				gameRoom.print();
				
				JSONObject updateJsonData = new JSONObject();
				updateJsonData.put("updateDataType", UpdateData.USER_DROP);
				updateJsonData.put("userDrop", cardsString);
				updateJsonData.put("gameRoomUpdate", gameRoom.forAllToJson());
				outData.put("updateData", updateJsonData);
				handler.prepareModerateMessage(TienLenMienBacHandler.PublishCommand.UPDATE_GAME, gameRoom, player, updateJsonData);
//				publishGame(gameRoom, player, outData);
			}
		
			outData.put("errorCode", errorCode);
		} catch (Exception exception) {
			Util.log("GameManager:drop:Exception:" + exception.toString());
		}
	}
	
	public static void fold(TienLenMienBacHandler handler, User player, String roomId, String userId, JSONObject outData) {
		try {
			int errorCode = ErrorCode.Tienlen.NULL;
			GameRoom gameRoom = (GameRoom)rooms.get(roomId);
			Util.log("drop " + roomId + " " + gameRoom);
			
			if (gameRoom != null) {
				gameRoom.fold(userId);
				gameRoom.print();
				
				JSONObject updateJsonData = new JSONObject();
				updateJsonData.put("updateDataType", UpdateData.USER_FOLD);
				updateJsonData.put("userFold", userId);
				updateJsonData.put("gameRoomUpdate", gameRoom.forAllToJson());
				outData.put("updateData", updateJsonData);
				handler.prepareModerateMessage(TienLenMienBacHandler.PublishCommand.UPDATE_GAME, gameRoom, player, updateJsonData);
//				publishGame(gameRoom, player, outData);
			}
			
			outData.put("errorCode", errorCode);
		} catch (Exception exception) {
			Util.log("GameManager:fold:Exception:" + exception.toString());
		}
	}
	
	public static void removeRoom(String roomId) {
		synchronized (roomId) {
			GameRoom room = rooms.get(roomId);
			
			if (room != null) {
				room.destroy();
				rooms.remove(room);
			}
		}
	}
	
	public static void update() {
		synchronized (rooms) {
			Enumeration<GameRoom> games = rooms.elements();
			
			while (games.hasMoreElements()) {
				games.nextElement().update();
			}
		}
	}
	
	private static String getMessageCommand(String commandId) {
		return GameId.TLMB + "." + commandId;
	}
	
	public static void publishGame(GameRoom gameRoom, User exceptUser, JSONObject jsonData) {
		Util.log("publishGame:" + gameRoom.roomId);
		
		try {
			Zone zone = gameExtension.getParentZone();
			SFSApi sfsApi = (SFSApi)gameExtension.getApi();
			
			if (jsonData.length() == 0) {
				jsonData.put("gameRoom", gameRoom.toJson());
			}
			
			Room joinedRoom = zone.getRoomByName(gameRoom.roomId);
			Util.log("publishGame:" + joinedRoom);
			
			if (joinedRoom != null) {
				ArrayList<ISession> sessions = (ArrayList<ISession>)joinedRoom.getSessionList();
				
				if (exceptUser != null) {
					sessions.remove(exceptUser.getSession());
				}
				
				ISFSObject sfsObject = new SFSObject();
				sfsObject.putByteArray("jsonData", Util.StringToBytesArray(jsonData.toString()));
				sfsApi.sendModeratorMessage(null, GameId.TLMB + "." + Command.UPDATE, sfsObject, sessions);
				Util.log("publishGame:sendModerateMessage");
			}
		} catch (Exception exception) {
			Util.log("GameManager:publishGame:Exception:" + exception.toString());
		}
	}
	
	public static void publishStartGame(GameRoom gameRoom, User exceptUser) {
		Util.log("publishStartGame:" + gameRoom.roomId);
		
		try {
			Zone zone = gameExtension.getParentZone();
			SFSApi sfsApi = (SFSApi)gameExtension.getApi();
			JSONObject jsonData = new JSONObject();
			
			Room joinedRoom = zone.getRoomByName(gameRoom.roomId);
			Util.log("publishStartGame:" + joinedRoom);
			
			if (joinedRoom != null) {
				ArrayList<User> users = (ArrayList<User>)joinedRoom.getUserList();
				ArrayList<ISession> sessions = new ArrayList<ISession>();
				
				if (exceptUser != null) {
					users.remove(exceptUser);
				}
				
				for (int i = 0; i < users.size(); i++) {
					jsonData = gameRoom.forUserToJson(users.get(i).getName());
					sessions.clear();
					sessions.add(users.get(i).getSession());
					
					ISFSObject sfsObject = new SFSObject();
					sfsObject.putByteArray("jsonData", Util.StringToBytesArray(jsonData.toString()));
					sfsApi.sendModeratorMessage(null, GameId.TLMB + "." + Command.START, sfsObject, sessions);
				}
				
				Util.log("publishGame:sendModerateMessage");
			}
		} catch (Exception exception) {
			Util.log("GameManager:publishGame:Exception:" + exception.toString());
		}
	}
}