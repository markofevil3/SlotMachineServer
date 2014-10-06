package com.yna.game.smartfox;

import org.json.JSONObject;

import com.smartfoxserver.v2.annotations.MultiHandler;
import com.smartfoxserver.v2.api.SFSApi;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.Zone;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.extensions.SFSExtension;
import com.yna.game.common.Util;

@MultiHandler
public class ClientRequestHandler extends BaseClientRequestHandler {
	protected Zone zone;
	protected SFSExtension extension;
	protected SFSApi sfsApi;
	
	@Override
	public void handleClientRequest(User player, ISFSObject params) {
		trace("handleClientRequest:" + player);
		extension = getParentExtension();
		zone = extension.getParentZone();
		sfsApi = (SFSApi)getApi();
		
		ISFSObject out = new SFSObject();
		JSONObject outData = new JSONObject();
		String requestId = params.getUtfString(SFSExtension.MULTIHANDLER_REQUEST_ID);
		String commandId = GetCommandId(requestId);
		handleRequest(commandId, player, params, outData);
		out.putByteArray("jsonData", Util.StringToBytesArray(outData.toString()));
		extension.send(requestId, out, player);
		trace("handleClientRequest:done");
		handleAfterRequest();
	}
	
	protected void handleRequest(String commandId, User player, ISFSObject params, JSONObject out) {
	}
	
	protected void handleAfterRequest() {
	}
	
	private String GetCommandId(String requestId) {
		String[] arr = requestId.split("\\.");
		return arr[arr.length - 1];
	}
}