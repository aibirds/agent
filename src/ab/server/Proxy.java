/*****************************************************************************
** ANGRYBIRDS AI AGENT FRAMEWORK
** Copyright (c) 2013,XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
**  Sahan Abeyasinghe, Jim Keys, Kar-Wai Lim, Zain Mubashir,  Andrew Wang, Peng Zhang
** All rights reserved.
**This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
**To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
*or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
*****************************************************************************/

package ab.server;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.SynchronousQueue;
import java.util.HashMap;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import ab.server.ProxyMessage;

/* Proxy ------------------------------------------------------------------ */

public class Proxy extends WebSocketServer {
    private Long id = 0L;
    private HashMap<Long, ProxyResult<?>> results = new HashMap<Long, ProxyResult<?>>();
    
    private class ProxyResult<T> {
        public ProxyMessage<T> message;
        public SynchronousQueue<Object> queue = new SynchronousQueue<Object>();
    }
	
    public Proxy(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
    }

    public Proxy(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        onOpen();
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        onClose();
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        JSONArray j = (JSONArray) JSONValue.parse(message);
        Long id = (Long) j.get(0);
        JSONObject data = (JSONObject) j.get(1);
        
        ProxyResult<?> result = results.get(id);
	
        if (result != null) {
            results.remove(id);
            try {
                result.queue.put(result.message.gotResponse(data));
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }
    
    @SuppressWarnings("unchecked")
    public <T> T send(ProxyMessage<T> message) {
        JSONArray a = new JSONArray();
        a.add(id);
        a.add(message.getMessageName());
        a.add(message.getJSON());
        
        ProxyResult<T> result = new ProxyResult<T>();
        result.message = message;
        results.put(id, result);
	
        for (WebSocket conn : connections()) {	
            conn.send(a.toJSONString());
        }
        
        id++;
	
        try {
            return (T)result.queue.take();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    public void onOpen() { }
    
    public void onClose() { }
    
    public void waitForClients(int numClients) {
        while (connections().size() < numClients) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
    }
}
