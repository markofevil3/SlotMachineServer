package com.yna.game.task;

import java.io.File;

import com.yna.game.common.Util;

public abstract class FileWatcher {
	private long timestamp;
	private File file;

	public FileWatcher(String filePath) {
		this.file = new File(filePath);
		this.timestamp = file.lastModified();
	}

	protected abstract void onChange(File file);

	public final void run() {
		long timestamp = file.lastModified();
		if (this.timestamp != timestamp) {
			this.timestamp = timestamp;
			onChange(file);
		}
	}
}