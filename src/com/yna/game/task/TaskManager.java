package com.yna.game.task;

import java.io.File;

import com.yna.game.slotmachine.models.SlotCombinationDragon;
import com.yna.game.slotmachine.models.SlotCombinationPirate;
import com.yna.game.slotmachine.models.SlotCombinationZombie;
import com.yna.game.smartfox.AdminMessageManager;
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