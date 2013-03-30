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
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.imageio.*;
import javax.swing.*;
import ab.vision.*;

public class ShowDebuggingImage {

    private static int _saveCount = 0;

    static class ImagePanel extends JPanel implements KeyListener, MouseListener {
        protected JFrame _parent;
        protected Image _img = null;
        protected Popup _tip = null;
        protected int[][] _meta = null;
        protected Boolean _highlightMode = false;
        protected int _highlightIndex = -1;

        public Boolean bWaitingForKey = false;

        public ImagePanel(JFrame parent) {
            _parent = parent;
            addKeyListener(this);
            addMouseListener(this);
            setDoubleBuffered(true);
        }

        public void refresh(Image img) {
            refresh(img, null);
        }

        public void refresh(Image img, int[][] meta) {
            _img = img;
            _meta = meta;
            this.repaint();
        }

        public void paint(Graphics g) {
            if (_img != null) {
                if ((_meta != null) && (_highlightIndex != -1)) {
                    BufferedImage canvas = VisionUtils.highlightRegions(_img, _meta, _highlightIndex, Color.RED);
                    g.drawImage(canvas, 0, 0, null);
                } else {
                    g.drawImage(_img, 0, 0, null);
                }
            }
        }

        public void keyPressed(KeyEvent key) {

            // process key
            if (key.getKeyCode() == key.VK_ENTER) {
                _parent.setVisible(false);
                _parent.dispose();

            } else if (key.getKeyCode() == key.VK_ESCAPE) {
                System.exit(0);

            } else if (key.getKeyCode() == key.VK_D) {
                String imgFilename = String.format("img%04d.png", _saveCount);
                System.out.println("saving image to " + imgFilename);
                BufferedImage bi = new BufferedImage(_img.getWidth(null), _img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = bi.createGraphics();
                g2d.drawImage(_img, 0, 0, null);
                g2d.dispose();
                try {
                    ImageIO.write(bi, "png", new File(imgFilename));
                } catch (IOException e) {
                    System.err.println("failed to save image " + imgFilename);
                    e.printStackTrace();
                }

                if (_meta != null) {
                    String metaFilename = String.format("meta%04d.txt", _saveCount);
                    System.out.println("saving meta-data to " + metaFilename);
                    try {
                        PrintWriter ofs = new PrintWriter(new FileWriter(metaFilename));
                        for (int i = 0; i < _meta.length; i++) {
                            for (int j = 0; j < _meta[i].length; j++) {
                                if (j > 0) ofs.print(' ');
                                ofs.print(_meta[i][j]);
                            }
                            ofs.println();
                        }
                        ofs.close();
                        
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                _saveCount += 1;

            } else if (key.getKeyCode() == key.VK_H) {
                // toggle highlight mode
                if (_highlightMode) {
                    _highlightMode = false;
                    this.repaint();
                } else {
                    _highlightMode = true;
                    _highlightIndex = -1;
                }

            } else if (key.getKeyCode() == key.VK_S) {
                String imgFilename = String.format("img%04d.png", _saveCount);
                System.out.println("saving image to " + imgFilename);
                BufferedImage bi = new BufferedImage(_img.getWidth(null), _img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = bi.createGraphics();
                g2d.drawImage(_img, 0, 0, null);
                g2d.dispose();
                try {
                    ImageIO.write(bi, "png", new File(imgFilename));
                } catch (IOException e) {
                    System.err.println("failed to save image " + imgFilename);
                    e.printStackTrace();
                }
                _saveCount += 1;
            }

            // check if usercode is waiting for a keypress
            if (bWaitingForKey) {
                bWaitingForKey = false;
                return;
            }
        }

        public void keyTyped(KeyEvent key) { }
        public void keyReleased(KeyEvent key) { }

        public void mousePressed(MouseEvent e) { }
        public void mouseReleased(MouseEvent e) { }

        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) { }

        public void mouseClicked(MouseEvent e) {
            if (_tip == null) {
                JToolTip toolTip = this.createToolTip();
                if (_meta == null) {
                    toolTip.setTipText("(" + e.getX() + ", " + e.getY() + ")");
                } else {
                    toolTip.setTipText("(" + e.getX() + ", " + e.getY() + "): " +
                        _meta[e.getY()][e.getX()]);

                    if (_highlightMode) {
                        _highlightIndex = _meta[e.getY()][e.getX()];
                        this.repaint();                      
                    }
                }

                Point p = new Point(e.getX(), e.getY());
                SwingUtilities.convertPointToScreen(p, this);
                _tip = PopupFactory.getSharedInstance().getPopup(this, toolTip, p.x, p.y);
                _tip.show();
                toolTip.addMouseListener(new MouseAdapter(){
                        public void mouseClicked(MouseEvent e){
                            if (_highlightMode) {
                                _highlightIndex = -1;
                                repaint();
                            }
                            _tip.hide();
                            _tip = null;
                        }
                    });
            } else {
                if (_highlightMode) {
                    _highlightIndex = -1;
                    this.repaint();
                }
                _tip.hide();
                _tip = null;
            }
        }
    }

    protected JFrame frame;
    protected ImagePanel panel;

    public ShowDebuggingImage(String name, Image img, int[][] meta) {

        frame = new JFrame(name);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel = new ImagePanel(frame);
        frame.getContentPane().add(panel);

        frame.pack();
        Insets insets = frame.getInsets();
        frame.setSize(img.getWidth(null) + insets.left + insets.right,
            img.getHeight(null) + insets.top + insets.bottom);
        frame.setVisible(true);

        panel.refresh(img, meta);
        panel.requestFocus();
    }

    public ShowDebuggingImage(String name, Image img) {
        this(name, img, null);
    }

    // set new image
    public void refresh(Image img) {
        panel.refresh(img);
    }

    // set new image and meta information for tooltip
    public void refresh(Image img, int[][] meta) {
        panel.refresh(img, meta);
    }

    // close the window
    public void close() {
	frame.setVisible(false);
	frame.dispose();
    }

    // wait for user input
    public void waitForKeyPress() {
        panel.bWaitingForKey = true;
        while (panel.bWaitingForKey) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
    }
}
