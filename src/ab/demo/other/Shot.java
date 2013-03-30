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



public class Shot {
	
	private int x;
	private int y;
	private int dx;
	private int dy;
	public int getDx() {
		return dx;
	}


	public void setDx(int dx) {
		this.dx = dx;
	}


	public int getDy() {
		return dy;
	}


	public Shot(int x, int y, int dx, int dy, int t_shot, int t_tap) {
		super();
		this.x = x;
		this.y = y;
		this.dx = dx;
		this.dy = dy;
		this.t_shot = t_shot;
		this.t_tap = t_tap;
	}
	
	public Shot(int x, int y, int dx, int dy, int t_shot) {
		super();
		this.x = x;
		this.y = y;
		this.dx = dx;
		this.dy = dy;
		this.t_shot = t_shot;
	}


	public void setDy(int dy) {
		this.dy = dy;
	}
	private int t_shot;
	private int t_tap;
public Shot()
{
	
}


public Shot(int x, int y, int t_shot, int t_tap) {
	super();
	this.x = x;
	this.y = y;
	this.t_shot = t_shot;
	this.t_tap = t_tap;
}


public int getX() {
	return x;
}


public void setX(int x) {
	this.x = x;
}


public int getY() {
	return y;
}


public void setY(int y) {
	this.y = y;
}


public int getT_shot() {
	return t_shot;
}


public void setT_shot(int t_shot) {
	this.t_shot = t_shot;
}


public int getT_tap() {
	return t_tap;
}


public void setT_tap(int t_tap) {
	this.t_tap = t_tap;
}


public String toString()
{
	String result = "";
	if(x==0 && y==0)
	{	
	 if(t_tap!=0)	
		result+= "tap at:  "  + t_tap;
	}
		else
		result += "Shoot from: ("+ (x + dx)+ "  " + (y + dy) +" )" + " at time  " + t_shot ;	
	
   return  result;

}

}
