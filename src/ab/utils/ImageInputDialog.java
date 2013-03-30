/*****************************************************************************
** ANGRYBIRDS AI AGENT FRAMEWORK
** Copyright (c) 2013,XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
**  Sahan Abeyasinghe, Jim Keys, Kar-Wai Lim, Zain Mubashir,  Andrew Wang, Peng Zhang
** All rights reserved.
**This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
**To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
*or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
*****************************************************************************/

package ab.utils;

import java.io.*;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.imageio.*;
import javax.swing.*;
import ab.vision.*;

public class ImageInputDialog {

    static class ImagePanel extends JPanel implements KeyListener, MouseListener {
        protected JFrame _parent;
        protected Image _img = null;

        public List<Point> mouseClicks = null;
        public Boolean bRunning = false;

        public ImagePanel(JFrame parent) {
            _parent = parent;
            mouseClicks = new ArrayList<Point>();
            addKeyListener(this);
            addMouseListener(this);
            setDoubleBuffered(true);
        }

        public void refresh(Image img) {
            _img = img;
            this.repaint();
        }

        public void paint(Graphics g) {

            // draw the image
            if (_img != null) {
                g.drawImage(_img, 0, 0, null);
            }

            // draw the points
            if (bRunning) {
                g.setColor(Color.YELLOW);
                for (Point p : mouseClicks) {
                    g.fillOval(p.x - 2, p.y - 2, 4, 4);
                }
                g.setColor(Color.RED);
                for (Point p : mouseClicks) {
                    g.drawOval(p.x - 2, p.y - 2, 4, 4);
                }
            }
        }

        public void keyPressed(KeyEvent key) {
            if (key.getKeyCode() == key.VK_ENTER) {
                bRunning = false;
            } else if (key.getKeyCode() == key.VK_ESCAPE) {
                mouseClicks.clear();
                bRunning = false;
            }
        }

        public void keyTyped(KeyEvent key) { }
        public void keyReleased(KeyEvent key) { }

        public void mousePressed(MouseEvent e) { }
        public void mouseReleased(MouseEvent e) { }

        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) { }

        public void mouseClicked(MouseEvent e) {
            if (bRunning) {
                mouseClicks.add(new Point(e.getX(), e.getY()));
                this.repaint();
            }
        }
    }

    protected JFrame frame;
    protected ImagePanel panel;

    // create an input image dialog
    public ImageInputDialog(String name, Image img) {

        frame = new JFrame(name);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel = new ImagePanel(frame);
        frame.getContentPane().add(panel);

        frame.pack();
        Insets insets = frame.getInsets();
        frame.setSize(img.getWidth(null) + insets.left + insets.right,
            img.getHeight(null) + insets.top + insets.bottom);
        frame.setVisible(true);
        frame.setResizable(false);

        panel.refresh(img);
        panel.requestFocus();
    }

    // set new image
    public void refresh(Image img) {
        panel.refresh(img);
    }

    // close the window
    public void close() {
	frame.setVisible(false);
	frame.dispose();
    }

    // get points
    public List<Point> getPointsInput() {

        panel.mouseClicks.clear();
        panel.bRunning = true;
        while (panel.bRunning && panel.mouseClicks.size() < 8) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }

        return panel.mouseClicks;
    }

    // get bounding box
    public Rectangle getBoxInput() {
        // TODO
        return null;
    }
}
