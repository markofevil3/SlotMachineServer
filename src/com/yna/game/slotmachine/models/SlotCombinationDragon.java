package com.yna.game.slotmachine.models;

import java.util.Arrays;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import com.smartfoxserver.v2.api.SFSApi;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.RoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSRoomVariable;
import com.yna.game.common.Util;

public class SlotCombinationDragon {
  public static Random random;
  public static int[] ITEM_RATES = new int[] {3, 8, 12, 11, 11, 10, 9, 8, 8, 6, 14};
  public static int[] SPECIAL_ITEM_RATES = new int[] {3, 0, 0, 10, 14, 18, 16, 16, 14, 9, 0};  
  public static int[][] PAYOUTS = new int[][] {
    // item 0 - ignore
    { 0, 0, 0, 0, 0 },
    // item 1
    { 0, 1, 3, 10, 85 },
    // item 2
    { 0, 0, 15, 30, 150 },
    // item 3
    { 0, 0, 20, 40, 250 },
    // item 4
    { 0, 0, 25, 50, 400 },
    // item 5
    { 0, 0, 30, 70, 550 },
    // item 6
    { 0, 0, 35, 80, 650 },
    // item 7
    { 0, 0, 45, 100, 800 },
    // item 8
    { 0, 0, 75, 175, 1250 },
    // item 9
    { 0, 0, 100, 200, 1750 },
    // special item
    { 0, 0, 5, 7, 15 }
  };
  
	public static final int DRAGON_FIRE = 0;
	public static final int DRAGON_ICE = 1;
	public static final int DRAGON_DARK = 2;

  public static int[] DRAGON_HP = new int[] { 500000, 700000, 1000000};
	
	public static void Init() {
  	random = new Random();
	}
	
	public static JSONObject SetGameVariable(User player, Room room, SFSApi sfsApi) {
		int dIndex = SpawnDragon();
		RoomVariable dragonIndex = new SFSRoomVariable("dIndex", dIndex);
    RoomVariable dragonHP = new SFSRoomVariable("dHP", DRAGON_HP[dIndex]);
    sfsApi.setRoomVariables(null, room, Arrays.asList(dragonIndex, dragonHP));
    JSONObject jsonData = new JSONObject();
    try {
			jsonData.put("dIndex", dIndex);
	    jsonData.put("dHP", DRAGON_HP[dIndex]);
	    jsonData.put("dMaxHP", DRAGON_HP[dIndex]);
		} catch (JSONException e) {
			Util.log("SlotCombinationDragon SetGameVariable " + e.toString());
		}
    return jsonData;
	}
	
	public static JSONObject UpdateGameVariable(User player, Room room, SFSApi sfsApi, JSONObject jsonData) {
    try {
			int dHP = room.getVariable("dHP").getIntValue();
			int damage = jsonData.getInt("totalWin");
			if (damage > 0) {
				dHP = Math.max(0, dHP - damage);
		    RoomVariable dragonHP = new SFSRoomVariable("dHP", dHP);
		    sfsApi.setRoomVariables(null, room, Arrays.asList(dragonHP), false, false, false);
		    jsonData.put("dHP", dHP);
				ISFSObject out = new SFSObject();
				out.putByteArray("jsonData", Util.StringToBytesArray(jsonData.toString()));
				out.putUtfString("cmd", Command.SLOT_PLAY);
				out.putUtfString("message", "");
		    sfsApi.sendPublicMessage(room, player, "Admin", out);
			}
		} catch (JSONException e) {
			Util.log("SlotCombinationDragon UpdateGameVariable " + e.toString());
		}
    return null;
	}
	
	public static JSONObject GetGameVariable(User player, Room room) {
    JSONObject jsonData = new JSONObject();
    try {
    	int dIndex = room.getVariable("dIndex").getIntValue(); 
			jsonData.put("dIndex", dIndex);
	    jsonData.put("dHP", room.getVariable("dHP").getIntValue());
	    jsonData.put("dMaxHP", DRAGON_HP[dIndex]);
		} catch (JSONException e) {
			Util.log("SlotCombinationDragon GetGameVariable " + e.toString());
		}
    return jsonData;
	}
	
	public static int SpawnDragon() {
		return random.nextInt(DRAGON_HP.length);
	}
}
