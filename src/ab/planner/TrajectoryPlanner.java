/*****************************************************************************
** ANGRYBIRDS AI AGENT FRAMEWORK
** Copyright (c) 2013,XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
**  Sahan Abeyasinghe, Jim Keys, Kar-Wai Lim, Zain Mubashir,  Andrew Wang, Peng Zhang
** All rights reserved.
**This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
**To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
*or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
*****************************************************************************/
package ab.planner;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.util.Random;

/* TrajectoryPlanner ------------------------------------------------------ */

public class TrajectoryPlanner {


    public static double x_offset = 0.5;
    public static double y_offset = 0.65;
    
    //                                         10,    15,    20,    25,    30,    35,    40,    45,    50,    55,    60,    65,    70
    private static double _launchAngle [] =   {0.13,  0.215, 0.296, 0.381, 0.476, 0.567, 0.657, 0.741, 0.832, 0.924, 1.014, 1.106, 1.197};
    private static double _changeAngle [] =   {0.052, 0.057, 0.063, 0.066, 0.056, 0.054, 0.050, 0.053, 0.052, 0.047, 0.043, 0.038, 0.034};
    private static double _launchVelocity[] = {2.9,   2.88,  2.866, 2.838, 2.810, 2.800, 2.790, 2.773, 2.753, 2.749, 2.744, 2.736, 2.728};
    
    // write angle changes to a file
    private PrintWriter out;

    // the velocity used in trajectory prediction
    private double _velocity;
    
    // the angle in previous velocity change
    private double _prevAngle;
    
    // the adjustment to angles
    private double _angleAdjust;
    
    // random angle
    private Random _r;

    // create a trajectory planner object
    public TrajectoryPlanner() {
        // set default velocity and angle adjustment
        _velocity = 1.00;
        _angleAdjust = 1.00;
        _prevAngle = 0;
        _r = new Random();
        
        try {
            //out = new PrintWriter(new FileWriter("output"));
        }
        catch (Exception e)
        {}
    }

    // finds the active bird, i.e., the one in the slingshot
    public Rectangle findActiveBird(List<Rectangle> birds) {
        // assumes that the active bird is the bird at the highest position
        Rectangle activeBird = null;
        for (Rectangle r : birds) {
            if ((activeBird == null) || (activeBird.y > r.y)) {
                activeBird = r;
            }
        }

        return activeBird;
    }

    // fit a quadratic to the given points and adjuct parameters
    // All coordinates are not normalised
    //      pts - list of points the trajectory passed through
    //      slingshot - bounding box of the slingshot
    //      releasePoint - where the bird was released from
    public void adjustTrajectory(final List<Point> pts, Rectangle slingshot, Point releasePoint)
    {
        double Sx2 = 0.0;
        double Sx3 = 0.0;
        double Sx4 = 0.0;
        double Syx = 0.0;
        double Syx2 = 0.0;

        // find scene scale and reference point
        double sceneScale = getSceneScale(slingshot);
        Point refPoint = getReferencePoint(slingshot);


        for (Point p : pts) {                
            // normalise the points
            double x = (p.x - refPoint.x) / sceneScale;
            double y = (p.y - refPoint.y) / sceneScale;
            
            final double x2 = x * x;
            Sx2 += x2;
            Sx3 += x * x2;
            Sx4 += x2 * x2;
            Syx += y * x;
            Syx2 += y * x2;
        }

        final double a = (Syx2 * Sx2 - Syx * Sx3) / (Sx4 * Sx2 - Sx3 * Sx3);
        final double b = (Syx - a * Sx3) / Sx2;
        
        // launch angle
        double theta = -Math.atan2(refPoint.y - releasePoint.y, refPoint.x - releasePoint.x);
        
        // actual angle
        double theta2 = -Math.atan2(b, 1);
        
        // find initial velocity
        double ux = Math.sqrt(0.5/a);
        double uy = ux * b;
        
        // adjust the velocity
        adjustVelocity(Math.sqrt(ux*ux + uy*uy), theta);
        
        //_angleAdjust = adjustAngle(Math.atan2(b, 1) - theta;
                 
        System.out.println("\nvelocity changed to: " + _velocity);
        
        //out.println(String.format("%.3f\t\t%.3f\t\t%.3f", Math.atan2(b, 1), (Math.atan2(b, 1) - theta), Math.sqrt(ux*ux + uy*uy)));
        //out.flush();
    }

    // predicts a trajectory
    public List<Point> predictTrajectory(Rectangle slingshot, Point launchPoint, int x_max) {

        // get slingshot reference point
        Point slingLocation = getReferencePoint(slingshot);

        // launch vector
        Point launchVector = new Point(slingLocation.x - launchPoint.x, launchPoint.y - slingLocation.y);
        if (launchVector.x < slingshot.width) {
            return new ArrayList<Point>();
        }

        // estimate scene scale
        double sceneScale = getSceneScale(slingshot);
        double theta = launchToActual(Math.atan2(launchVector.y, launchVector.x));
        double velocity = getVelocity(theta);
        
        //System.out.println("launch angle " + Math.toDegrees(theta));
        
        // initial velocities
        double u_x = velocity * Math.cos(theta);
        double u_y = velocity * Math.sin(theta);

        // the normalised coefficients
        double a = -0.5 / (u_x * u_x);
        double b = u_y / u_x;

        //System.out.println("plot trajectory: " + a + "x^2 + " + b + "x");

	    ArrayList<Point> trajectory = new ArrayList<Point>();
        for (int x = 0; x < x_max; x++) {
            double xn = x / sceneScale;
            int y = slingLocation.y - (int)((a * xn * xn + b * xn) * sceneScale);
            trajectory.add(new Point(x + slingLocation.x, y));
        }

	    return trajectory;
    }
    
    // estimate launch point given a desired target point
    // if there are two launch point for the target, they are both returned in
    // the list {lower point, higher point)
    // Note - angles greater than 80 are not considered due to their low initial velocity
    public ArrayList<Point> estimateLaunchPoint(Rectangle slingshot, Point targetPoint) {
        
        // calculate relative position of the target (normalised)
        double scale = getSceneScale(slingshot);
        
        Point ref = getReferencePoint(slingshot);
            
        double x = (targetPoint.x - ref.x) / scale;
        double y = -(targetPoint.y - ref.y) / scale;
        
        double bestError = 1000;
        double theta1 = 0;
        double theta2 = 0;
        
        // search tangents from -0.5 to 1.0
        for (double tangent = -0.5; tangent < 1.0; tangent += 0.0001)
        {
            double theta = Math.atan(tangent);
            double velocity = getVelocity(theta);
            
            // initial velocities
            double u_x = velocity * Math.cos(theta);
            double u_y = velocity * Math.sin(theta);
            
            // the normalised coefficients
            double a = -0.5 / (u_x * u_x);
            double b = u_y / u_x;
            
            // the error in y-coordinate
            double error = Math.abs(a*x*x + b*x - y);
            if (error < bestError)
            {
                theta1 = theta;
                bestError = error;
            }
        }
        
        bestError = 1000;
        
        for (double tangent = -1.0; tangent < 4.0; tangent += 0.0001)
        {
            double theta = Math.atan(tangent);
            double velocity = getVelocity(theta);
            
            // initial velocities
            double u_x = velocity * Math.cos(theta);
            double u_y = velocity * Math.sin(theta);
            
            // the normalised coefficients
            double a = -0.5 / (u_x * u_x);
            double b = u_y / u_x;
            
            // the error in y-coordinate
            double error = Math.abs(a*x*x + b*x - y);
            if (error < bestError)
            {
                theta2 = theta;
                bestError = error;
            }
        }
        
        theta1 = actualToLaunch(theta1);
        theta2 = actualToLaunch(theta2);
        
        //System.out.println("Two angles: " + Math.toDegrees(theta1) + ", " + Math.toDegrees(theta2));
            
        // add launch points to the list
        ArrayList<Point> pts = new ArrayList<Point>();
        pts.add(findReleasePoint(slingshot, theta1));
        
        // add the higher point if it is below 75 degrees and not same as first
        if (theta2 < Math.toRadians(75) && theta2 != theta1)
            pts.add(findReleasePoint(slingshot, theta2));
        
        return pts;
    }
   
    // plot a trajectory given the release point
    public BufferedImage plotTrajectory(BufferedImage canvas, Rectangle slingshot, Point releasePoint) {
        List<Point> trajectory = predictTrajectory(slingshot, releasePoint, canvas.getWidth(null) - slingshot.x);
        
        // draw estimated trajectory
        Graphics2D g2d = canvas.createGraphics();
        g2d.setColor(Color.RED);
        for (Point p : trajectory) {
            if ((p.y > 0) && (p.y < canvas.getHeight(null))) {
                g2d.drawRect(p.x, p.y, 1, 1);
            }
        }
        
        return canvas; 
    }

    // plot trajectory given the bounding box of the active bird
    public BufferedImage plotTrajectory(BufferedImage canvas, Rectangle slingshot, Rectangle activeBird) {
        
        // get active bird location
        Point bird = new Point((int)(activeBird.x + 0.5 * activeBird.width),
            (int)(activeBird.y + 0.85 * activeBird.height));
        
        
        return plotTrajectory(canvas, slingshot, bird);
    }

    // find the release point given the sling and launch angle
    //      theta - the launch angle in radians (positive means up)
    public Point findReleasePoint(Rectangle sling, double theta)
    {
        double mag = sling.height * 10;
        Point ref = getReferencePoint(sling);
        Point release = new Point((int)(ref.x - mag * Math.cos(theta)), (int)(ref.y + mag * Math.sin(theta)));
        
        return release;
    }
    
    // find the reference point given the sling
    public Point getReferencePoint(Rectangle sling)
    {
        Point p = new Point((int)(sling.x + x_offset * sling.width), (int)(sling.y + y_offset * sling.width));
        return p;
    }
    
    // return scene scale determined by the sling size
    public double getSceneScale(Rectangle sling)
    {
        return sling.height + sling.width;
    }
    
    // take the initial angle of the desired trajectory and return the launch angle required
    public double actualToLaunch(double theta)
    {
        for (int i = 1; i < _launchAngle.length; i++)
        {
            if (theta > _launchAngle[i-1] && theta < _launchAngle[i])
                return theta + _changeAngle[i-1];
        }
        return theta + _changeAngle[_launchAngle.length-1];
    }
    
    // take the launch angle and return the actual angle of the resulting trajectory
    public double launchToActual(double theta)
    {
        for (int i = 1; i < _launchAngle.length; i++)
        {
            if (theta > _launchAngle[i-1] && theta < _launchAngle[i])
                return theta - _changeAngle[i-1];
        }
        return theta - _changeAngle[_launchAngle.length-1];
    }
    

    //get release angle 
    public double getReleaseAngle(Rectangle sling, Point releasePoint )
    {
        Point ref = getReferencePoint(sling);
        
        return -Math.atan2(ref.y - releasePoint.y, ref.x - releasePoint.x);
    }
    
    //adjust the velocity for this scene
    private void adjustVelocity(double v, double theta)
    {    
        int i = 0;
        while (i < _launchVelocity.length && theta > _launchAngle[i])
            i++;
        if (i == 0)
            i = 1;
        
        double temp = v / _launchVelocity[i-1];
            
        // avoid setting velocity to NaN
        if (temp != temp)
            return;
            
        // ignore very large changes
        if (temp > 1.1 || temp < 0.9)
            return;
                   
        if (theta > Math.toRadians(40))    
            _velocity = temp;
        else
            _velocity = temp * 0.6 + _velocity * 0.4;
                            
        _prevAngle = theta;
    }
    
    // get the velocity for the desired angle
    private double getVelocity(double theta)
    {
        if (theta < _launchAngle[0])
            return _velocity * _launchVelocity[0];
        
        for (int i = 1; i < _launchAngle.length; i++)
        {
            if (theta < _launchAngle[i])
                return _velocity * _launchVelocity[i-1];
        }
        
        return _velocity * _launchVelocity[_launchVelocity.length-1];
    }        
}
