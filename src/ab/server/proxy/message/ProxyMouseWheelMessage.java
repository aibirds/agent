/*****************************************************************************
** ANGRYBIRDS AI AGENT FRAMEWORK
** Copyright (c) 2013,XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
**  Sahan Abeyasinghe, Jim Keys, Kar-Wai Lim, Zain Mubashir,  Andrew Wang, Peng Zhang
** All rights reserved.
**This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
**To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
*or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
*****************************************************************************/
package ab.server.proxy.message;

import org.json.simple.JSONObject;

import ab.server.ProxyMessage;

public class ProxyMouseWheelMessage implements ProxyMessage<Object> {
	private int delta;
	
	/**
	 * Simulate a scroll of the mouse wheel
	 * 
	 * @param delta the direction to scroll (-1 = up, 1 = down)
	 */
	public ProxyMouseWheelMessage(int delta) {
		this.delta = delta;
	}
	
	@Override
	public String getMessageName() {
		return "mousewheel";
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getJSON() {
		JSONObject o = new JSONObject();
		o.put("delta", delta);
		return o;
	}
	
	@Override
	public Object gotResponse(JSONObject data) {
		return new Object();
	}
}
