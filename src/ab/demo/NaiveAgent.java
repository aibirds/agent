/*****************************************************************************
** ANGRYBIRDS AI AGENT FRAMEWORK
** Copyright (c) 2013, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
**  Sahan Abeyasinghe,Jim Keys, Kar-Wai Lim, Zain Mubashir, Andrew Wang, Peng Zhang
** All rights reserved.
**This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
**To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
*or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
*****************************************************************************/
package ab.demo;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ab.demo.other.ActionRobot;
import ab.demo.other.Env;
import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

public class NaiveAgent implements Runnable{
   
	private int focus_x;
	private int focus_y;

	private ActionRobot ar;
	public  int currentLevel  = 1;
	TrajectoryPlanner tp;
	
	private boolean firstShot;
	private Point prevTarget;
	
    public NaiveAgent() 
    {
    	 ar = new ActionRobot();
    	 tp = new TrajectoryPlanner();
    	 prevTarget = null;
    	 firstShot = true;
    	 //--- go to the Poached Eggs episode level selection page ---
    	 ActionRobot.GoFromMainMenuToLevelSelection();
    	 
    }
   public int getCurrent_level() {
		return currentLevel;
	}


	public void setCurrent_level(int current_level) {
		this.currentLevel = current_level;
	}


    public void run() 
   {

	   ar.loadLevel(currentLevel);
	   while(true)
	   {
		   GameState state = solve();
		   if(state ==  GameState.WON)
		   {
			   		ar.loadLevel(++currentLevel);
			   		
			   		// make a new trajectory planner whenever a new level is entered
			   		tp = new TrajectoryPlanner();
			   		
			   		// first shot on this level, try high shot first
			   		firstShot = true;
		   }
		   else
			   if(state == GameState.LOST)
			   {
				   System.out.println("restart");
				   ar.restartLevel();
			   }
			   else
				   if(state == GameState.LEVEL_SELECTION)
				   {
					   System.out.println("unexpected level selection page, go to the lasts current level : " + currentLevel);
					   ar.loadLevel(currentLevel);
				   }
				   else
					   if(state == GameState.MAIN_MENU)
					   {
						   System.out.println("unexpected main menu page, go to the lasts current level : " + currentLevel);
						   ActionRobot.GoFromMainMenuToLevelSelection();
						   ar.loadLevel(currentLevel);
					   }
					   else
						   if(state  == GameState.EPISODE_MENU)
						   {
							   System.out.println("unexpected episode menu page, go to the lasts current level : " + currentLevel);
							   ActionRobot.GoFromMainMenuToLevelSelection();
							   ar.loadLevel(currentLevel);
						   }

	   }

   }
   
   private double distance(Point p1, Point p2)
   {
        return Math.sqrt((double)((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)));
   }
   
   public GameState solve()  

   {

       //capture Image
	   BufferedImage screenshot = ActionRobot.doScreenShot();


	   // process image
       Vision vision = new Vision(screenshot);
    
       Rectangle sling = vision.findSlingshot();

       
       while (sling == null && ar.checkState() ==  GameState.PLAYING)
       {
    	   		 System.out.println("no slingshot detected. Please remove pop up or zoom out");
    	   	     ar.fullyZoom();
    	      	  screenshot = ActionRobot.doScreenShot();
    	      	  vision = new Vision(screenshot);
    	      	  sling =   vision.findSlingshot();
       }
       
       List<Rectangle> red_birds = vision.findRedBirds();
	    List<Rectangle> blue_birds = vision.findBlueBirds();
	    List<Rectangle> yellow_birds = vision.findYellowBirds();
	    List<Rectangle> pigs =   vision.findPigs();
   	   int bird_count = 0;
	   bird_count = red_birds.size() + blue_birds.size() + yellow_birds.size();

	   System.out.println("...found " + pigs.size() + " pigs and " + bird_count + " birds");
       GameState state = ar.checkState();
       
       //if there is a sling, then play, otherwise just skip. 
       if( sling!=null)
       {
    	   ar.fullyZoom();
    	   if(!pigs.isEmpty())
    	   {
    		  
			// initialize a shot list
			 ArrayList<Shot> shots = new ArrayList<Shot>();
			 Point releasePoint;
			{
				//random pick up a pig
				Random r = new Random();

				int index = r.nextInt(pigs.size());
				Rectangle pig = pigs.get(index);
				Point _tpt =  new Point((int)pig.getCenterX(),(int)pig.getCenterY());

				System.out.println("the target point is：  " + _tpt);
				
				// if the target is very close to before, randomly choose a point near it
				if (prevTarget != null && distance(prevTarget, _tpt) < 10)
				{
				    double _angle = r.nextDouble() * Math.PI * 2;
				    _tpt.x = _tpt.x + (int) (Math.cos(_angle) * 10);
				    _tpt.y = _tpt.y + (int) (Math.sin(_angle) * 10);
				    System.out.println("Randomly changing to " + _tpt);
				}
				
                prevTarget = new Point(_tpt.x, _tpt.y);

				// estimate the trajectory
				ArrayList<Point> pts = tp.estimateLaunchPoint(sling, _tpt);

                // do a high shot when entering a level to find an accurate velocity
                if (firstShot && pts.size() > 1)
                {
                    releasePoint = pts.get(1);
                }
                else if (pts.size() == 1)
                    releasePoint = pts.get(0);
                else
                {
                    //System.out.println("first shot " + firstShot);    
				    // randomly choose between the trajectories, with a 1 in 6 chance of choosing the high one
				    if (r.nextInt(6) == 0)
				        releasePoint = pts.get(1);
				    else
				        releasePoint = pts.get(0);
				}
				Point refPoint = tp.getReferencePoint(sling);
				/* Get the center of the active bird */
				 focus_x = (int) ((Env.getFocuslist().containsKey(currentLevel))?Env.getFocuslist().get(currentLevel).getX():refPoint.x);
				 focus_y = (int)  ((Env.getFocuslist().containsKey(currentLevel))?Env.getFocuslist().get(currentLevel).getY():refPoint.y);
				System.out.println("the release point is: " + releasePoint);
				   /*=========== Get the release point from the trajectory prediction module====*/  
		        System.out.println("Shoot!!");
				if(releasePoint !=null)
				{
				  double releaseAngle = tp.getReleaseAngle(sling, releasePoint);
				  System.out.println(" The release angle is : " + Math.toDegrees(releaseAngle));
				  int base = 0;
				  if(releaseAngle > Math.PI/4)
					  base = 1400;
				  else
					  base = 550;
			       int tap_time = (int)(base + Math.random() * 1500); 
			       shots.add(new Shot(focus_x, focus_y,  (int)releasePoint.getX() - focus_x, (int)releasePoint.getY() - focus_y,0,tap_time));
				}
				else
					System.err.println("Out of Knowledge");
			}

			//check whether the slingshot is changed. the change of the slingshot indicates a change in the scale.
			  {
				  ar.fullyZoom();
				  screenshot = ActionRobot.doScreenShot();
				  vision = new Vision(screenshot);
				  Rectangle _sling =   vision.findSlingshot();
                 if(sling.equals(_sling))
                 {
                	 state =   ar.shootWithStateInfoReturned(shots);
                     // update parameters after a shot is made
                     if (state == GameState.PLAYING)
                     {
                        screenshot = ActionRobot.doScreenShot();
                        vision = new Vision(screenshot);
                        List<Point> traj = vision.findTrajPoints();
                        tp.adjustTrajectory(traj, sling, releasePoint);
                        firstShot = false;
                        //System.out.println("first shot false");
                     }
                 }
                 else
                	 System.out.println("scale is changed, can not execute the shot, will re-segement the image");
			  }

    	   }
    	   
    	   
    	   }
           return state;
   }
	public static void main(String args[]) 
	   {

		   NaiveAgent na = new  NaiveAgent();
		   if(args.length > 0)
			   na.currentLevel = Integer.parseInt(args[0]);
	    	na.run();




	   }
}
