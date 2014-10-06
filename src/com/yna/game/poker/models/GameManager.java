package com.yna.game.poker.models;

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
import com.yna.game.smartfox.handler.PokerHandler;
import com.yna.game.tienlen.models.GameRoom;

public class GameManager {
	public static final int MAX_ROOMS = 500;
	public static final int ROOM_NAME = 10;
	
	public static Hashtable<String, GameRoom> rooms;
	public static SFSExtension gameExtension;
	
	public static void init(SFSExtension extension) {
		rooms = new Hashtable<String, GameRoom>();
		gameExtension = extension;
	}
	
	public static void update() {
		synchronized (rooms) {
			Enumeration<GameRoom> games = rooms.elements();
			
			while (games.hasMoreElements()) {
				games.nextElement().update();
			}
		}
	}
	
	public static void destroy() {
		Enumeration<GameRoom> games = rooms.elements();
		
		while (games.hasMoreElements()) {
			games.nextElement().destroy();
		}
		
		rooms.clear();
	}
}
