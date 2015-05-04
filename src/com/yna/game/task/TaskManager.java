package com.yna.game.task;

import java.io.File;

import com.yna.game.task.FileWatcher;
import com.yna.game.common.GameConstants;

public class TaskManager implements Runnable {

	private static FileWatcher testFileWatcher = null;

	public static void Init() {
		GameConstants.Init();
		testFileWatcher = new FileWatcher(System.getProperty("user.dir") + GameConstants.DATA_FILE_PATH) {
			@Override
			protected void onChange(File file) {
				GameConstants.Init();
			}
		};
	}
	
	@Override
	public void run() {
		testFileWatcher.run();
	}
}