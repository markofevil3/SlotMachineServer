package com.yna.game.slotmachine.models;

import java.util.Arrays;
import java.util.List;
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
import com.yna.game.smartfox.UserManager;

public class SlotCombinationPirate extends SlotCombinations {
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
  
//  private static final int BOSS_FIRE = 0;
//	private static final int BOSS_ICE = 1;
//	private static final int BOSS_DARK = 2;

	private static int[] BOSS_HP = new int[] { 500000, 700000, 1000000};
	
  private static int[][] BOSS_DROP_CASH = new int[][] {
    // BOSS_FIRE
    { 50000, 100000, 150000 },
    // BOSS_ICE
    { 150000, 200000, 250000 },
    // BOSS_DARK
    { 300000, 350000, 400000 },
  };
  
  private static int[][] BOSS_DROP_GEM = new int[][] {
    // BOSS_FIRE
    { 0, 0, 1 },
    // BOSS_ICE
    { 0, 1, 2 },
    // BOSS_DARK
    { 1, 2, 3 },
  };
  
  public int[] GetBossHp() {
  	return BOSS_HP;
  }
  
  public int[][] GetBossDropCash() {
  	return BOSS_DROP_CASH;
  }
  
  public int[][] GetBossDropGem() {
  	return BOSS_DROP_GEM;
  }
  
  public int[][] GetPayOuts() {
  	return PAYOUTS;
  }
  
  public int[] GetItemRate() {
  	return ITEM_RATES;
  }
  
  public int[] GetSpecialItemRate() {
  	return SPECIAL_ITEM_RATES;
  }
  
  public SlotCombinationPirate() {
  	Util.log("SlotCombinationPirate Init");
  	random = new Random();
  }
  
//	public void Init() {
//  	super.Init();
//	}
	
//	public JSONObject SetGameVariable(User player, Room room, SFSApi sfsApi) {
//		int dIndex = SpawnBoss(room.containsVariable("dIndex") ? room.getVariable("dIndex").getIntValue() : -1);
//		RoomVariable dragonIndex = new SFSRoomVariable("dIndex", dIndex);
//    RoomVariable dragonHP = new SFSRoomVariable("dHP", BOSS_HP[dIndex]);
//    sfsApi.setRoomVariables(null, room, Arrays.asList(dragonIndex, dragonHP), false, false, false);
//    JSONObject jsonData = new JSONObject();
//    try {
//			jsonData.put("dIndex", dIndex);
//	    jsonData.put("dHP", BOSS_HP[dIndex]);
//	    jsonData.put("dMaxHP", BOSS_HP[dIndex]);
//		} catch (JSONException e) {
//			Util.log("SlotCombinationPirate SetGameVariable " + e.toString());
//		}
//    return jsonData;
//	}
//	
//	public JSONObject UpdateGameVariable(User player, Room room, SFSApi sfsApi, JSONObject out, int totalWin) {
//    try {
//			int dHP = room.getVariable("dHP").getIntValue();
//			int dIndex = room.getVariable("dIndex").getIntValue();
//			int damage = totalWin;
//			if (damage > 0) {
//		    JSONObject dataToOthers = new JSONObject();
//				dHP = Math.max(0, dHP - damage);
//				if (dHP == 0) {
//					// random treasure
//					int dropIndex = RandomDrop();
//					JSONArray dropItems = new JSONArray();
//					dropItems.put(BOSS_DROP_CASH[dIndex][dropIndex]);
//					dropItems.put(BOSS_DROP_GEM[dIndex][dropIndex]);
//					out.put("dropItems", dropItems);
//					dataToOthers.put("dropItems", dropItems);
//					List<User> usersInRoom = room.getUserList();
//					for (int i = 0; i < usersInRoom.size(); i++) {
//						String mUsername = usersInRoom.get(i).getName();
//						if (mUsername != player.getName()) {
//							UserManager.updatePlayerCashGemAndKill(mUsername, BOSS_DROP_CASH[dIndex][dropIndex], BOSS_DROP_GEM[dIndex][dropIndex], 1);
//						} else {
//							UserManager.updatePlayerBossKill(mUsername, 1);
//						}
//					}
//					// spawn new BOSS
//					JSONObject newBoss = SetGameVariable(player, room, sfsApi);
//					out.put("newBoss", newBoss);
//					dataToOthers.put("newBoss", newBoss);
//				} else {
//			    RoomVariable dragonHP = new SFSRoomVariable("dHP", dHP);
//			    sfsApi.setRoomVariables(null, room, Arrays.asList(dragonHP), false, false, false);
//				}
//				out.put("dHP", dHP);
//				out.put("dIndex", dIndex);
//				dataToOthers.put("dHP", dHP);
//				dataToOthers.put("dIndex", dIndex);
//				dataToOthers.put("items", out.getJSONArray("items"));
//				dataToOthers.put("wGold", out.getJSONArray("wGold"));
//				dataToOthers.put("nL", out.getInt("nL"));
//				ISFSObject outPublicMess = new SFSObject();
//				outPublicMess.putByteArray("jsonData", Util.StringToBytesArray(dataToOthers.toString()));
//				outPublicMess.putUtfString("cmd", Command.SLOT_PLAY);
//				outPublicMess.putUtfString("message", "");
//		    sfsApi.sendPublicMessage(room, player, "Admin", outPublicMess);
//		    return out;
//			}
//		} catch (JSONException e) {
//			Util.log("SlotCombinationPirate UpdateGameVariable " + e.toString());
//		}
//    return out;
//	}
//	
//	public JSONObject GetGameVariable(User player, Room room) {
//    JSONObject jsonData = new JSONObject();
//    try {
//    	int dIndex = room.getVariable("dIndex").getIntValue(); 
//			jsonData.put("dIndex", dIndex);
//	    jsonData.put("dHP", room.getVariable("dHP").getIntValue());
//	    jsonData.put("dMaxHP", BOSS_HP[dIndex]);
//		} catch (JSONException e) {
//			Util.log("SlotCombinationPirate GetGameVariable " + e.toString());
//		}
//    return jsonData;
//	}
	
//	private static int SpawnBoss(int crtBossIndex) {
//		if (crtBossIndex != -1) {
//			return (crtBossIndex + 1) % BOSS_HP.length;
//		}
//		return random.nextInt(BOSS_HP.length);
//	}
//	
//	private static int RandomDrop() {
//		return random.nextInt(BOSS_DROP_CASH[0].length);
//	}
}
