package com.yna.game.task;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import com.yna.game.slotmachine.models.SlotCombinationDragon;
import com.yna.game.slotmachine.models.SlotCombinationPirate;
import com.yna.game.slotmachine.models.SlotCombinationZombie;
import com.yna.game.smartfox.AdminMessageManager;
import com.yna.game.smartfox.UserManager;
import com.yna.game.task.FileWatcher;
import com.yna.game.common.GameConstants;

public class TaskManager implements Runnable {

	private static FileWatcher gameConstantsFileWatcher = null;
	private static FileWatcher adminMessageFileWatcher = null;
	private static FileWatcher slotZombieFileWatcher = null;
	private static FileWatcher slotDragonFileWatcher = null;
	private static FileWatcher slotPirateFileWatcher = null;

	public static void Init() {
		String dataFolderPath = System.getProperty("user.dir");

		GameConstants.init();
		gameConstantsFileWatcher = new FileWatcher(dataFolderPath + GameConstants.GAMECONSTANTS_FILE_PATH) {
			@Override
			protected void onChange(File file) {
				GameConstants.init();
			}
		};
		
		AdminMessageManager.init();
		adminMessageFileWatcher = new FileWatcher(dataFolderPath + AdminMessageManager.ADMIN_MESSAGES_FILE_PATH) {
			@Override
			protected void onChange(File file) {
				AdminMessageManager.init();
			}
		};
		slotZombieFileWatcher = new FileWatcher(dataFolderPath + SlotCombinationZombie.DATA_FILE_PATH) {
			@Override
			protected void onChange(File file) {
				SlotCombinationZombie.initGameData();
			}
		};
		slotDragonFileWatcher = new FileWatcher(dataFolderPath + SlotCombinationDragon.DATA_FILE_PATH) {
			@Override
			protected void onChange(File file) {
				SlotCombinationDragon.initGameData();
			}
		};
		slotPirateFileWatcher = new FileWatcher(dataFolderPath + SlotCombinationPirate.DATA_FILE_PATH) {
			@Override
			protected void onChange(File file) {
				SlotCombinationPirate.initGameData();
			}
		};
		
		// TO DO: reload time interval if changed from text file
		Timer timer = new Timer();
    timer.scheduleAtFixedRate(new ClearExpiredCacheUsers(), GameConstants.CLEAR_CACHE_USERS_INTERVAL_MILI, GameConstants.CLEAR_CACHE_USERS_INTERVAL_MILI);
	}
	
	@Override
	public void run() {
		gameConstantsFileWatcher.run();
		adminMessageFileWatcher.run();
		slotZombieFileWatcher.run();
		slotDragonFileWatcher.run();
		slotPirateFileWatcher.run();
	}
}

class ClearExpiredCacheUsers extends TimerTask{
  //This task will repeat every five seconds
  public void run(){
  	UserManager.saveAndClearExpiredCacheUser();
  }
}