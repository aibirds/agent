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

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;

import ab.server.ProxyMessage;

// request a screenshot from the game
public class ProxyScreenshotMessage implements ProxyMessage<byte[]> {
    @Override
    public String getMessageName() {
        return "screenshot";
    }

    @Override
    public JSONObject getJSON() {
        return new JSONObject();
    }

    @Override
    public byte[] gotResponse(JSONObject data) {
        String imageStr = (String) data.get("data");
        imageStr = imageStr.split(",", 2)[1];
        byte[] imageBytes = Base64.decodeBase64(imageStr);
        return imageBytes;
    }
}
