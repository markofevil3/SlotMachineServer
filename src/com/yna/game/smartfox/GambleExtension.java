package com.yna.game.smartfox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.api.CreateRoomSettings;
import com.smartfoxserver.v2.api.ISFSApi;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.db.IDBManager;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.SFSRoomRemoveMode;
import com.smartfoxserver.v2.entities.Zone;
import com.smartfoxserver.v2.entities.variables.RoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSRoomVariable;
import com.smartfoxserver.v2.extensions.SFSExtension;
import com.yna.game.common.GameConstants;
import com.yna.game.slotmachine.models.GameType;
import com.yna.game.slotmachine.models.SlotCombinationDragon;
import com.yna.game.slotmachine.models.SlotCombinationPirate;
import com.yna.game.slotmachine.models.SlotCombinationZombie;
import com.yna.game.slotmachine.models.SlotCombinations;
import com.yna.game.smartfox.handler.SlotMachineHandler;
import com.yna.game.smartfox.handler.TienLenMienBacHandler;
import com.yna.game.smartfox.handler.UserRequestHandler;
import com.yna.game.task.TaskManager;
import com.yna.game.tienlen.models.TienLenManager;

public class GambleExtension extends SFSExtension {
	
	private String LOBBY_GROUP_ID = "lobby";
	
	@Override
	public void init() {
		TienLenManager.init(this);
//		SlotCombinations.Init();
		// TO DO: create new slotmachine instance and init here
		trace("-------------------GambleExtension Init all slot machines------------------------");
		GameType.slotPirateInstance = new SlotCombinationPirate();
		GameType.slotDragonInstance = new SlotCombinationDragon();
		GameType.slotZombieInstance = new SlotCombinationZombie();
		trace("-------------------GambleExtension Init all slot machines-------DONE-------------");
		trace("-------------------GambleExtension Create LOBBY ROOMS------------------------");
		createLobbyRooms();
		trace("-------------------GambleExtension Create LOBBY ROOMS----DONE-------------");

		UserRequestHandler.init();
		
		addRequestHandler(GameId.TLMB, TienLenMienBacHandler.class);
		addRequestHandler(GameId.USER, UserRequestHandler.class);
		addRequestHandler(GameId.SLOT_MACHINE, SlotMachineHandler.class);

		addEventHandler(SFSEventType.USER_LOGIN, GambleEventHandler.class);
		addEventHandler(SFSEventType.USER_JOIN_ZONE, GambleEventHandler.class);
		addEventHandler(SFSEventType.USER_DISCONNECT, GambleEventHandler.class);
		addEventHandler(SFSEventType.USER_LEAVE_ROOM, GambleEventHandler.class);
		addEventHandler(SFSEventType.PUBLIC_MESSAGE, GambleEventHandler.class);
		addEventHandler(SFSEventType.BUDDY_LIST_INIT, GambleEventHandler.class);
		addEventHandler(SFSEventType.USER_LOGOUT, GambleEventHandler.class);
		addEventHandler(SFSEventType.SERVER_READY, GambleEventHandler.class);
	}

	private void createLobbyRooms() {
		Zone zone = getParentZone();
//		private static final String FRUIT_LOBBY_ROOM = "fruitLobby";
//		private static final String HALLOWEEN_LOBBY_ROOM = "halloweenLobby";
//		private static final String DRAGON_LOBBY_ROOM = "dragonLobby";
		IDBManager dbManager = zone.getDBManager();
		Connection connection = null;
		PreparedStatement selectStatement = null;
		ResultSet selectResultSet = null;
		int jackpotDragon = 0;
		int jackpotPirate = 0;
		int jackpotZombie = 0;
    try {
	  	// Grab a connection from the DBManager connection pool
	    connection = dbManager.getConnection();
	    // Throw error - cant get any connection
	    if (connection == null) {
	  		trace("createLobbyRooms NO CONNECTION AVAILABLE");
	    } else {
	  		selectStatement = connection.prepareStatement("SELECT * FROM jackpots WHERE (gType=? OR gType=? OR gType=?) AND username IS NULL");
	  		selectStatement.setString(1, GameType.SLOT_TYPE_DRAGON);
	  		selectStatement.setString(2, GameType.SLOT_TYPE_PIRATE);
	  		selectStatement.setString(3, GameType.SLOT_TYPE_ZOMBIE);
	      // Execute query
		    selectResultSet = selectStatement.executeQuery();
		    while (selectResultSet.next()) {
		    	switch (selectResultSet.getString("gType")) {
		    	case GameType.SLOT_TYPE_DRAGON:
		    		jackpotDragon = selectResultSet.getInt("val");
		    		break;
		    	case GameType.SLOT_TYPE_PIRATE:
		    		jackpotPirate = selectResultSet.getInt("val");
		    		break;
		    	case GameType.SLOT_TYPE_ZOMBIE:
		    		jackpotZombie = selectResultSet.getInt("val");
		    		break;
		    	}
		    }
	    }
    }
    // Username was not found
    catch (SQLException e) {
  		trace("createLobbyRooms SQLException | JSONException: " + e.toString());
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
		
		CreateRoomSettings roomSettings = new CreateRoomSettings();
		roomSettings.setMaxUsers(GameConstants.LOBBY_MAX_USERS);
		roomSettings.setGroupId(LOBBY_GROUP_ID);
		roomSettings.setAutoRemoveMode(SFSRoomRemoveMode.NEVER_REMOVE );
		roomSettings.setGame(false);
		roomSettings.setDynamic(false);
		roomSettings.setHidden(true);
		try {
			// Set lobby DRAGON room
			ISFSApi api = getApi();
			Room createdRoom;
			RoomVariable jackpot;
			if (zone.getRoomByName(GameType.GetLoobyRoom(GameType.SLOT_TYPE_DRAGON)) == null) {
				roomSettings.setName(GameType.GetLoobyRoom(GameType.SLOT_TYPE_DRAGON));
				createdRoom = api.createRoom(zone, roomSettings, null, false, null, false, false);
				jackpot = new SFSRoomVariable("jackpot", jackpotDragon);
				createdRoom.setVariable(jackpot);
				trace("DRAGON " + jackpotDragon);
			}
			// Set lobby PIRATE room
			if (zone.getRoomByName(GameType.GetLoobyRoom(GameType.SLOT_TYPE_PIRATE)) == null) {
				roomSettings.setName(GameType.GetLoobyRoom(GameType.SLOT_TYPE_PIRATE));
				createdRoom = api.createRoom(zone, roomSettings, null, false, null, false, false);
				jackpot = new SFSRoomVariable("jackpot", jackpotPirate);
				createdRoom.setVariable(jackpot);
				trace("PIRATE " + jackpotPirate);
			}
			// Set lobby ZOMBIE room
			if (zone.getRoomByName(GameType.GetLoobyRoom(GameType.SLOT_TYPE_ZOMBIE)) == null) {
				roomSettings.setName(GameType.GetLoobyRoom(GameType.SLOT_TYPE_ZOMBIE));
				createdRoom = api.createRoom(zone, roomSettings, null, false, null, false, false);
				jackpot = new SFSRoomVariable("jackpot", jackpotZombie);
				createdRoom.setVariable(jackpot);
				trace("ZOMBIE " + jackpotZombie);
			}
//			// Set lobby HALLOWEEN room
//			roomSettings.setName(GameType.GetLoobyRoom(GameType.SLOT_TYPE_HALLOWEEN));
//			createdRoom = api.createRoom(zone, roomSettings, null, false, null, false, false);
//			jackpot = new SFSRoomVariable("jackpot", jackpotHalloween);
//			createdRoom.setVariable(jackpot);
//			trace("HALLOWEEN " + jackpotHalloween);
//			// Set lobby FRUITS room
//			roomSettings.setName(GameType.GetLoobyRoom(GameType.SLOT_TYPE_FRUITS));
//			createdRoom = api.createRoom(zone, roomSettings, null, false, null, false, false);
//			jackpot = new SFSRoomVariable("jackpot", jackpotFruit);
//			createdRoom.setVariable(jackpot);
//			trace("FRUITS " + jackpotFruit);
		} catch (Exception exception) {
			trace("createLobbyRooms:Exception:" + exception.toString());
		}
	}
	
	// TO DO: save jackpot to database interval
	// TO DO: when 1 user got jackpot, save to jackpots table, create new jackpot
	
	// TO DO: clean up, save data before stop server
	@Override
	public void destroy() {
		super.destroy();
		removeEventHandler(SFSEventType.USER_LOGIN);
		removeEventHandler(SFSEventType.USER_JOIN_ZONE);
		removeEventHandler(SFSEventType.USER_DISCONNECT);
		removeEventHandler(SFSEventType.USER_LEAVE_ROOM);
		removeEventHandler(SFSEventType.PUBLIC_MESSAGE);
		removeEventHandler(SFSEventType.BUDDY_LIST_INIT);
		removeEventHandler(SFSEventType.USER_LOGOUT);

		removeRequestHandler(GameId.TLMB);
		removeRequestHandler(GameId.USER);
		removeRequestHandler(GameId.SLOT_MACHINE);
		TienLenManager.destroy();
		AdminMessageManager.destroy();
	}
}
