package com.yna.game.smartfox;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.extensions.SFSExtension;
import com.yna.game.slotmachine.models.SlotCombinations;
import com.yna.game.smartfox.handler.SlotMachineHandler;
import com.yna.game.smartfox.handler.TienLenMienBacHandler;
import com.yna.game.smartfox.handler.UserRequestHandler;
import com.yna.game.task.TaskManager;
import com.yna.game.tienlen.models.TienLenManager;

public class GambleExtension extends SFSExtension {
	ScheduledFuture<?> taskManager;
	
	@Override
	public void init() {
		TienLenManager.init(this);
		SlotCombinations.Init();
		trace("GambleExtension Init");
		SmartFoxServer sfs = SmartFoxServer.getInstance();
		sfs.getEventManager().setThreadPoolSize(20);
		taskManager = sfs.getTaskScheduler().scheduleAtFixedRate(new TaskManager(), 0, 1, TimeUnit.SECONDS);
		
		UserRequestHandler.init();
		
		addRequestHandler(GameId.TLMB, TienLenMienBacHandler.class);
		addRequestHandler(GameId.USER, UserRequestHandler.class);
		addRequestHandler(GameId.SLOT_MACHINE, SlotMachineHandler.class);

		addEventHandler(SFSEventType.USER_LOGIN, GambleEventHandler.class);
		addEventHandler(SFSEventType.USER_JOIN_ZONE, GambleEventHandler.class);
		addEventHandler(SFSEventType.USER_DISCONNECT, GambleEventHandler.class);
		addEventHandler(SFSEventType.USER_LEAVE_ROOM, GambleEventHandler.class);
		addEventHandler(SFSEventType.PUBLIC_MESSAGE, GambleEventHandler.class);
		addEventHandler(SFSEventType.BUDDY_LIST_INIT, GambleEventHandler.class);
	}

	public void destroy() {
		removeEventHandler(SFSEventType.USER_LOGIN);
		removeEventHandler(SFSEventType.USER_JOIN_ZONE);
		removeEventHandler(SFSEventType.USER_DISCONNECT);
		removeEventHandler(SFSEventType.USER_LEAVE_ROOM);
		removeEventHandler(SFSEventType.PUBLIC_MESSAGE);
		removeRequestHandler(GameId.TLMB);
		removeRequestHandler(GameId.USER);
		removeRequestHandler(GameId.SLOT_MACHINE);
		TienLenManager.destroy();
	}
}
