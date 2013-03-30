package ab.demo;

import ab.utils.GameImageRecorder;
import ab.vision.TestVision;

/*****************************************************************************
** ANGRYBIRDS AI AGENT FRAMEWORK
** Copyright (c) 2013, XiaoYu (Gary) Ge, Jochen Renz,Stephen Gould,
**  Sahan Abeyasinghe,Jim Keys, Kar-Wai Lim, Zain Mubashir,  Andrew Wang, Peng Zhang
** All rights reserved.
**This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
**To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
*or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
*****************************************************************************/

public class MainEntry {
	// the entry of the software.
public static void main(String args[])
{
				String command = "";
				if(args.length > 0)
				{
				   command = args[0];
					if (args.length == 1 && command.equalsIgnoreCase("-na"))
					{
						NaiveAgent na = new  NaiveAgent();
						na.run();
					}
					else 
						if (args.length == 2 && command.equalsIgnoreCase("-na"))
						{
							  NaiveAgent na = new  NaiveAgent();
							  if(! args[1].equalsIgnoreCase("-showSeg"))
							  {
									int initialLevel = 1;
									try{
										initialLevel = Integer.parseInt(args[1]);
									}
									catch (NumberFormatException e)
									{
										System.out.println("wrong level number, will use the default one");
									}
								  na.currentLevel = initialLevel;
					              na.run();
							  }
							  else
							  {
								  Thread nathre = new Thread(na);
								   nathre.start();
								   Thread thre = new Thread(new TestVision());
							      thre.start();
							  }
						} 
						else if (args.length == 3 && args[2].equalsIgnoreCase("-showSeg") && command.equalsIgnoreCase("-na"))
						{
							NaiveAgent na = new  NaiveAgent();
							int initialLevel = 1;
							try{
								initialLevel = Integer.parseInt(args[1]);
							}
							catch (NumberFormatException e)
							{
								System.out.println("wrong level number, will use the default one");
							}
							na.currentLevel = initialLevel;
							  Thread nathre = new Thread(na);
							   nathre.start();
							   Thread thre = new Thread(new TestVision());
						      thre.start();
							
						}
								
					else if(command.equalsIgnoreCase("-showSeg"))
					{
						String[] param = {};
						TestVision.main(param);
					}
					else if (command.equalsIgnoreCase("-showTraj"))
					{
						String[] param = {};
						//TestTrajectory.main(param);
						abTrajectory.main(param);
					} 
					else if (command.equalsIgnoreCase("-recordImg"))
					{
					
						 if(args.length < 2)
							 System.out.println("please specify the directory");
						 else
							 {
							 	String[] param = {args[1]};   
							
							 	GameImageRecorder.main(param);
							 }
					}
					else 
					{
						System.out.println("Please input the correct command");
					}
				}
				else 
				{
					System.out.println("Please input the correct command");
				}
	
}
}
