package com.yna.game.slotmachine.models;

import java.util.Random;
import com.yna.game.common.Util;

public class SlotCombinationDragon extends SlotCombinations {
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
  
//  private static final int DRAGON_FIRE = 0;
//	private static final int DRAGON_ICE = 1;
//	private static final int DRAGON_DARK = 2;

	private static int[] BOSS_HP = new int[] { 5000000, 7000000, 10000000};
	
  private static int[][] BOSS_DROP_CASH = new int[][] {
    // BOSS_FIRE
    { 100000, 130000, 150000 },
    // BOSS_ICE
    { 160000, 180000, 200000 },
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
  
  public SlotCombinationDragon() {
  	Util.log("SlotCombinationDragon Init");
  	random = new Random();
  }
	
}
