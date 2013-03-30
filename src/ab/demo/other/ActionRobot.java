/*****************************************************************************
** ANGRYBIRDS AI AGENT FRAMEWORK
** Copyright (c) 2013,XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
**  Sahan Abeyasinghe, Jim Keys, Kar-Wai Lim, Zain Mubashir,  Andrew Wang, Peng Zhang
** All rights reserved.
**This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
**To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
*or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
*****************************************************************************/
package ab.demo.other;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import javax.imageio.ImageIO;

import ab.demo.util.StateUtil;
import ab.server.Proxy;
import ab.server.proxy.message.ProxyClickMessage;
import ab.server.proxy.message.ProxyMouseWheelMessage;
import ab.server.proxy.message.ProxyScreenshotMessage;
import ab.vision.GameStateExtractor.GameState;

/**
 *  Util class for basic functions
 *
 */
public class ActionRobot {
public static Proxy proxy;
public String level_status = "UNKNOWN";
public int current_score = 0;
private LoadingLevelSchema lls; 
private RestartLevelSchema rls;
private    ShootingSchema ss;

public ActionRobot()
{
if(proxy==null){	
	try {
		proxy = new Proxy(9000) {
			@Override
			public void onOpen() {
				System.out.println("Client connected");
			}

			@Override
			public void onClose() {
				System.out.println("Client disconnected");
			}
		};
		proxy.start();

		System.out.println("Server started on port: " + proxy.getPort());

		System.out.println("Waiting for client to connect");
		proxy.waitForClients(1);
		
		lls = new LoadingLevelSchema(proxy);
	    rls = new RestartLevelSchema(proxy);
	    ss = new ShootingSchema();
	    
	} catch (UnknownHostException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
}


public void restartLevel()
{
    rls.restartLevel();
}

public static void GoFromMainMenuToLevelSelection()
{
	  //--- go from the main menu to the episode menu
	  GameState state = StateUtil.checkCurrentState(proxy) ;
	while (state == GameState.MAIN_MENU){
	       
		  System.out.println("Go to the Episode Menu");
		   proxy.send(new ProxyClickMessage(305,277));
	       try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   state = StateUtil.checkCurrentState(proxy);
	}
	  //--- go from the episode menu to the level selection  menu
	  while( state == GameState.EPISODE_MENU)
			{
		       System.out.println("Select the Poached Eggs Episode");
		        proxy.send(new ProxyClickMessage(150,300));
		        state = StateUtil.checkCurrentState(proxy) ;
		        try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        state = StateUtil.checkCurrentState(proxy);
		  }
	 
}

public GameState shootWithStateInfoReturned(List<Shot> csc)
{
    ShootingSchema ss = new ShootingSchema();
	ss.shoot(proxy,csc);
	System.out.println("Shooting Completed");
    GameState state =  StateUtil.checkCurrentState(proxy); 
	return  state;	

}

public synchronized GameState checkState()
{
	GameState state =  StateUtil.checkCurrentState(proxy); 
		return  state;	
}

public boolean shoot(List<Shot> csc)
{
	ss.shoot(proxy,csc);
	System.out.println("Shooting Completed");
	return true;

}

public void loadLevel(int... i)
{
   int level = 1;
	if(i.length > 0)
	{
		level = i[0];
	}
    lls.loadLevel(level);

}

public void fullyZoom()
{
	 for (int k = 0; k < 15; k++) 
	   {
		   proxy.send(new ProxyMouseWheelMessage(-1));
	   }	
	 try {
		Thread.sleep(1000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
} 



public static synchronized BufferedImage doScreenShot() {
	byte[] imageBytes = proxy.send(new ProxyScreenshotMessage());
       BufferedImage image = null;
       try {
           image = ImageIO.read(new ByteArrayInputStream(imageBytes));
       } catch (IOException e) {
           // do something
       }

       return image;
   }

}
