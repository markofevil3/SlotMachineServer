package com.yna.game.task;

import java.io.File;

import com.yna.game.smartfox.AdminMessageManager;
import com.yna.game.task.FileWatcher;
import com.yna.game.common.GameConstants;

public class TaskManager implements Runnable {

	private static FileWatcher gameConstantsFileWatcher = null;
	private static FileWatcher adminMessageFileWatcher = null;

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
	}
	
	@Override
	public void run() {
		gameConstantsFileWatcher.run();
		adminMessageFileWatcher.run();
	}
}