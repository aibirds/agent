/*****************************************************************************
** ANGRYBIRDS AI AGENT FRAMEWORK
** Copyright (c) 2013,XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
**  Sahan Abeyasinghe, Jim Keys, Kar-Wai Lim, Zain Mubashir,  Andrew Wang, Peng Zhang
** All rights reserved.
**This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
**To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
*or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
*****************************************************************************/

package ab.demo.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import ab.server.Proxy;
import ab.server.proxy.message.ProxyScreenshotMessage;
import ab.vision.GameStateExtractor;
import ab.vision.GameStateExtractor.GameState;

/**
 * @author Gary
 *
 */
public class StateUtil {
	
	public static  GameState  checkCurrentState(Proxy proxy)
	{
	    System.out.println("check the game state:     ");
		 byte[] imageBytes = proxy.send(new ProxyScreenshotMessage());
		
	        BufferedImage image = null;
	        try {
	            image = ImageIO.read(new ByteArrayInputStream(imageBytes));
	        } catch (IOException e) {
	            // do something
	        }
	        GameStateExtractor gameStateExtractor = new GameStateExtractor();
	        GameStateExtractor.GameState state = gameStateExtractor.getGameState(image);
	        System.out.println("the game state is :     " + state);
		  return state;
	}
}
