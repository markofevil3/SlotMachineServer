package com.yna.game.task;

import com.yna.game.tienlen.models.TienLenManager;

public class TaskManager implements Runnable {

	@Override
	public void run() {
		TienLenManager.update();
	}
}