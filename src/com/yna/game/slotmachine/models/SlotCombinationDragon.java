package com.yna.game.slotmachine.models;

import java.util.Arrays;
import java.util.Random;

import org.json.JSONArray;
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
    { 0, 10, 30, 100, 850 },
    // item 2
    { 0, 0, 150, 300, 1500 },
    // item 3
    { 0, 0, 200, 400, 2500 },
    // item 4
    { 0, 0, 250, 500, 4000 },
    // item 5
    { 0, 0, 300, 700, 5500 },
    // item 6
    { 0, 0, 350, 800, 6500 },
    // item 7
    { 0, 0, 450, 1000, 8000 },
    // item 8
    { 0, 0, 750, 1750, 12500 },
    // item 9
    { 0, 0, 1000, 2000, 17500 },
    // special item
    { 0, 0, 5, 7, 15 }
  };
  
  private static final int DRAGON_FIRE = 0;
	private static final int DRAGON_ICE = 1;
	private static final int DRAGON_DARK = 2;

	private static int[] DRAGON_HP = new int[] { 500000, 700000, 1000000};
	
  private static int[][] DRAGON_DROP_CASH = new int[][] {
    // DRAGON_FIRE
    { 50000, 100000, 150000 },
    // DRAGON_ICE
    { 150000, 200000, 250000 },
    // DRAGON_DARK
    { 300000, 350000, 400000 },
  };
  
  private static int[][] DRAGON_DROP_GEM = new int[][] {
    // DRAGON_FIRE
    { 0, 0, 1 },
    // DRAGON_ICE
    { 0, 1, 2 },
    // DRAGON_DARK
    { 1, 2, 3 },
  };
  
  
	public static void Init() {
  	random = new Random();
	}
	
	public static JSONObject SetGameVariable(User player, Room room, SFSApi sfsApi) {
		int dIndex = SpawnDragon();
		RoomVariable dragonIndex = new SFSRoomVariable("dIndex", dIndex);
    RoomVariable dragonHP = new SFSRoomVariable("dHP", DRAGON_HP[dIndex]);
    sfsApi.setRoomVariables(null, room, Arrays.asList(dragonIndex, dragonHP), false, false, false);
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
		    // To do: spawn new dragon if old is dead, random treasure and add for users
				if (dHP == 0) {
					// random treasure
					int dropIndex = RandomDrop();
		    	int dIndex = room.getVariable("dIndex").getIntValue(); 
					JSONArray dropItems = new JSONArray();
					// TO do: add drop to user data
					dropItems.put(DRAGON_DROP_CASH[dIndex][dropIndex]);
					dropItems.put(DRAGON_DROP_GEM[dIndex][dropIndex]);
					jsonData.put("dropItems", dropItems);
					// spawn new dragon
					JSONObject newDragon = SetGameVariable(player, room, sfsApi);
					jsonData.put("newBoss", newDragon);
				} else {
			    RoomVariable dragonHP = new SFSRoomVariable("dHP", dHP);
			    sfsApi.setRoomVariables(null, room, Arrays.asList(dragonHP), false, false, false);
				}
		    jsonData.put("dHP", dHP);
				ISFSObject out = new SFSObject();
				out.putByteArray("jsonData", Util.StringToBytesArray(jsonData.toString()));
				out.putUtfString("cmd", Command.SLOT_PLAY);
				out.putUtfString("message", "");
		    sfsApi.sendPublicMessage(room, player, "Admin", out);
		    return jsonData;
			}
		} catch (JSONException e) {
			Util.log("SlotCombinationDragon UpdateGameVariable " + e.toString());
		}
    return new JSONObject();
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
	
	private static int SpawnDragon() {
		return random.nextInt(DRAGON_HP.length);
	}
	
	private static int RandomDrop() {
		return random.nextInt(DRAGON_DROP_CASH[0].length);
	}
}
