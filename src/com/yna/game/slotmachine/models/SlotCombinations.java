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

public class SlotCombinations {
	
	public class SlotItem {
		public static final int ITEM_WILD = 0;
		public static final int ITEM_1 = 1;
		public static final int ITEM_2 = 2;
		public static final int ITEM_3 = 3;
		public static final int ITEM_4 = 4;
		public static final int ITEM_5 = 5;
		public static final int ITEM_6 = 6;
		public static final int ITEM_7 = 7;
		public static final int ITEM_8 = 8;
		public static final int ITEM_9 = 9;
		public static final int ITEM_SPECIAL = 10;
		public static final int TOTAL = 11;
	}
	
  private int MAX_LINE = 9;
  private int NUM_REELS = 5;
  private int MAX_DISPLAY_ITEMS = 15;
  
  // Default value - each game will have different value in its own script
//  private static int[] ITEM_RATES = new int[] {3, 8, 12, 11, 11, 10, 9, 8, 8, 6, 14};
//  private static int[] SPECIAL_ITEM_RATES = new int[] {3, 0, 0, 10, 14, 18, 16, 16, 14, 9, 0};
  
  private int[] ITEM_RATES;
  private int[] SPECIAL_ITEM_RATES; 
  
  private static float randomValue;
  public Random random;
  
  public static int[][] COMBINATION = new int[][] {
    // line 1
    { 1, 4, 7, 10, 13 },
    // line 2
    { 0, 3, 6, 9, 12 },
    // line 3
    { 2, 5, 8, 11, 14 },
    // line 4
    { 0, 4, 8, 10, 12 },
    // line 5
    { 2, 4, 6, 10, 14 },
    // line 6
    { 0, 3, 7, 11, 14 },
    // line 7
    { 2, 5, 7, 9, 12 },
    // line 8
    { 1, 5, 7, 9, 13 },
    // line 9
    { 1, 3, 7, 11, 13 },
  };
  
  private int[][] PAYOUTS;
  
  // Default value - each game will have different value in its own script
//  public static int[][] PAYOUTS = new int[][] {
//    // item 0 - ignore
//    { 0, 0, 0, 0, 0 },
//    // item 1
//    { 0, 1, 3, 10, 85 },
//    // item 2
//    { 0, 0, 15, 30, 150 },
//    // item 3
//    { 0, 0, 20, 40, 250 },
//    // item 4
//    { 0, 0, 25, 50, 400 },
//    // item 5
//    { 0, 0, 30, 70, 550 },
//    // item 6
//    { 0, 0, 35, 80, 650 },
//    // item 7
//    { 0, 0, 45, 100, 800 },
//    // item 8
//    { 0, 0, 75, 175, 1250 },
//    // item 9
//    { 0, 0, 100, 200, 1750 },
//    // special item
//    { 0, 0, 5, 7, 15 }
//  };
  
  public int[] GetBossHp() {
  	return null;
  }
  
  public int[][] GetBossDropCash() {
  	return null;
  }
  
  public int[][] GetBossDropGem() {
  	return null;
  }
  
  public int[][] GetPayOuts() {
  	return null;
  }
  
  public int[] GetItemRate() {
  	return null;
  }
  
  public int[] GetSpecialItemRate() {
  	return null;
  }
  
  public void Init() {
  	
  }
  
//	public static void Init() {
//		Util.log("SlotCombinations - Init");
////  	random = new Random();
//  	SlotCombinationFruit.Init();
//  	SlotCombinationHalloween.Init();
//  	SlotCombinationDragon.Init();
//  	SlotCombinationPirate.Init();
//  }
    
  public int RandomItem(boolean isFreeSpin, String gameType) {
    float cap = 0;
    randomValue = random.nextFloat() * 100;
    
    if (isFreeSpin) {
      SPECIAL_ITEM_RATES = GetSpecialItemRate();
      for (int i = 0; i < SPECIAL_ITEM_RATES.length; i++) {
        if (randomValue <= cap + SPECIAL_ITEM_RATES[i]) {
          return i;
        } else {
          cap += SPECIAL_ITEM_RATES[i];
        }
      }
    } else {
    	ITEM_RATES = GetItemRate();
      for (int i = 0; i < ITEM_RATES.length; i++) {
        if (randomValue <= cap + ITEM_RATES[i]) {
          return i;
        } else {
          cap += ITEM_RATES[i];
        }
      }
    }

    return 1;
  }
  
  public int[] GenerateRandomItems(boolean isFreeSpin, String gameType) {
  	int[] resultData = new int[MAX_DISPLAY_ITEMS];
  	for (int i = 0; i < MAX_DISPLAY_ITEMS; i++) {
  		resultData[i] = RandomItem(isFreeSpin, gameType);
    }
  	return resultData;
  }
  
  // input data is array type of 15 items - output data is array winning gold of 9 lines
  public JSONObject CalculateCombination(int[] reelData, int numLines, int betPerLine, String gameType, JSONObject out) {
    int[] winningLineCount = new int[numLines];
    int[] winningLineType = new int[numLines];
    int[] winningGold = new int[numLines];
    boolean isJackpot = false; 
    PAYOUTS = GetPayOuts();
    for (int i = 0; i < numLines; i++) {
      for (int j = 0; j < NUM_REELS - 1; j++) {
        if (j == 0 && reelData[COMBINATION[i][j]] != SlotItem.ITEM_WILD) {
          winningLineCount[i]++;
          winningLineType[i] = reelData[COMBINATION[i][j]];
          continue;
        }
        if (reelData[COMBINATION[i][j]] == SlotItem.ITEM_WILD) {
          winningLineCount[i]++;
        } else {
          if (winningLineType[i] == 0) {
            winningLineCount[i]++;
            winningLineType[i] = reelData[COMBINATION[i][j]];
            continue;
          } else if (reelData[COMBINATION[i][j]] == winningLineType[i]){
            winningLineCount[i]++;
            continue;
          } else {
            break;
          }
        }
      }
      if (numLines == MAX_LINE && winningLineCount[i] == NUM_REELS) {
      	isJackpot = true;
      }
      if (winningLineType[i] != SlotItem.ITEM_SPECIAL) {
        winningGold[i] = PAYOUTS[winningLineType[i]][winningLineCount[i] - 1] * betPerLine;
      }
    }
    int specialCount = 0;
    for (int i = 0; i < reelData.length; i++) {
    	if (reelData[i] == SlotItem.ITEM_SPECIAL && specialCount < NUM_REELS) {
    		specialCount++;
    	}
    }
    try {
    	out.put("wGold", new JSONArray(winningGold));
    	out.put("nL", numLines);
    	out.put("isJP", isJackpot);
//	    results.put("isSpecial", specialCount > 0);
    	out.put("frCount", specialCount > 0 ? PAYOUTS[SlotItem.ITEM_SPECIAL][specialCount - 1] : 0);
		} catch (JSONException e) {
			Util.log("CalculateCombination JSONObject error: " + e.toString());
		}
    return out;
  }
  
	public JSONObject SetGameVariable(User player, Room room, SFSApi sfsApi) {
		int dIndex = SpawnBoss(room.containsVariable("dIndex") ? room.getVariable("dIndex").getIntValue() : -1);
		RoomVariable dragonIndex = new SFSRoomVariable("dIndex", dIndex);
    RoomVariable dragonHP = new SFSRoomVariable("dHP", GetBossHp()[dIndex]);
    sfsApi.setRoomVariables(null, room, Arrays.asList(dragonIndex, dragonHP), false, false, false);
    JSONObject jsonData = new JSONObject();
    try {
			jsonData.put("dIndex", dIndex);
	    jsonData.put("dHP", GetBossHp()[dIndex]);
	    jsonData.put("dMaxHP", GetBossHp()[dIndex]);
		} catch (JSONException e) {
			Util.log("SlotCombinationPirate SetGameVariable " + e.toString());
		}
    return jsonData;
	}
	
	public JSONObject UpdateGameVariable(User player, Room room, SFSApi sfsApi, JSONObject out, int totalWin) {
    try {
			int dHP = room.getVariable("dHP").getIntValue();
			int dIndex = room.getVariable("dIndex").getIntValue();
			int damage = totalWin;
			if (damage > 0) {
		    JSONObject dataToOthers = new JSONObject();
				dHP = Math.max(0, dHP - damage);
				if (dHP == 0) {
					// random treasure
					int dropIndex = RandomDrop();
					JSONArray dropItems = new JSONArray();
					dropItems.put(GetBossDropCash()[dIndex][dropIndex]);
					dropItems.put(GetBossDropGem()[dIndex][dropIndex]);
					out.put("dropItems", dropItems);
					dataToOthers.put("dropItems", dropItems);
					List<User> usersInRoom = room.getUserList();
					for (int i = 0; i < usersInRoom.size(); i++) {
						String mUsername = usersInRoom.get(i).getName();
						if (mUsername != player.getName()) {
							UserManager.updatePlayerCashGemAndKill(mUsername, GetBossDropCash()[dIndex][dropIndex], GetBossDropGem()[dIndex][dropIndex], 1);
						} else {
							UserManager.updatePlayerBossKill(mUsername, 1);
						}
					}
					// spawn new BOSS
					JSONObject newBoss = SetGameVariable(player, room, sfsApi);
					out.put("newBoss", newBoss);
					dataToOthers.put("newBoss", newBoss);
				} else {
			    RoomVariable dragonHP = new SFSRoomVariable("dHP", dHP);
			    sfsApi.setRoomVariables(null, room, Arrays.asList(dragonHP), false, false, false);
				}
				out.put("dHP", dHP);
				out.put("dIndex", dIndex);
				dataToOthers.put("dHP", dHP);
				dataToOthers.put("dIndex", dIndex);
				dataToOthers.put("items", out.getJSONArray("items"));
				dataToOthers.put("wGold", out.getJSONArray("wGold"));
				dataToOthers.put("nL", out.getInt("nL"));
				ISFSObject outPublicMess = new SFSObject();
				outPublicMess.putByteArray("jsonData", Util.StringToBytesArray(dataToOthers.toString()));
				outPublicMess.putUtfString("cmd", Command.SLOT_PLAY);
				outPublicMess.putUtfString("message", "");
		    sfsApi.sendPublicMessage(room, player, "Admin", outPublicMess);
		    return out;
			}
		} catch (JSONException e) {
			Util.log("SlotCombinationPirate UpdateGameVariable " + e.toString());
		}
    return out;
	}
	
	public JSONObject GetGameVariable(User player, Room room) {
    JSONObject jsonData = new JSONObject();
    try {
    	int dIndex = room.getVariable("dIndex").getIntValue(); 
			jsonData.put("dIndex", dIndex);
	    jsonData.put("dHP", room.getVariable("dHP").getIntValue());
	    jsonData.put("dMaxHP", GetBossHp()[dIndex]);
		} catch (JSONException e) {
			Util.log("SlotCombinationPirate GetGameVariable " + e.toString());
		}
    return jsonData;
	}
	
	public int SpawnBoss(int crtBossIndex) {
		if (crtBossIndex != -1) {
			return (crtBossIndex + 1) % GetBossHp().length;
		}
		return random.nextInt(GetBossHp().length);
	}
	
	public int RandomDrop() {
		return random.nextInt(GetBossDropCash()[0].length);
	}
}
