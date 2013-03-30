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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import ab.server.Proxy;
import ab.server.proxy.message.ProxyDragMessage;

public class ShootingSchema {
private LinkedList<Shot> shootings;
private boolean shootImmediately = true;

public ShootingSchema()
{
  shootings = new LinkedList<Shot>();	

}

public void shoot(final Proxy proxy,List<Shot> csc)
{
	int count = 0;
    for(Shot shot:csc)
    {
    	if(shot.getT_shot() != 0)
    		 shootImmediately = false;
    	if(shootImmediately)
    	{
    		int t_shot = 5000 * count++;
    		shootings.add(new Shot(shot.getX(),shot.getY(),shot.getDx(),shot.getDy(),t_shot));
    		if(shot.getT_tap() > 0)
        		shootings.add(new  Shot(0,0,0,0,shot.getT_tap() + t_shot));
    	}
    	else
    	{
    		shootings.add(new  Shot(shot.getX(),shot.getY(),shot.getDx(),shot.getDy(),shot.getT_shot()));
    		if(shot.getT_tap() > 0)
        		shootings.add(new Shot(0,0,0,0,shot.getT_tap() + shot.getT_shot()));
    	}
    	
    }
	
	Collections.sort(shootings, new Comparator<Shot>(){

		@Override
		public int compare(Shot arg0, Shot arg1) {
			
			return ((Integer)arg0.getT_shot()).compareTo(arg1.getT_shot());
		}
    	});
	if(!shootings.isEmpty())
	{
					 int start_time = shootings.getFirst().getT_shot();
			   for(Shot shot: shootings)
			   {
				   try {
				    Thread.sleep((shot.getT_shot() - start_time)==0?start_time:(shot.getT_shot()- start_time));
				    start_time =  shot.getT_shot();
					System.out.println(shot);
					proxy.send(new ProxyDragMessage(shot.getX(),shot.getY(),shot.getDx(),shot.getDy()));
				} catch (InterruptedException e) {
					System.out.println(" exception thrown from shooting schema ");
					e.printStackTrace();
				}
   }
System.out.println("wait 10 seconds to ensure all objects in the scene static"); 
try {
	Thread.sleep(10000);
} catch (InterruptedException e) {
	e.printStackTrace();
}
	}
}

}
