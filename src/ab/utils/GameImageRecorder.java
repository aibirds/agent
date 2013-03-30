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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;

import ab.server.Proxy;
import ab.server.proxy.message.ProxyScreenshotMessage;
import ab.vision.VisionUtils;

/* GameImageRecorder ------------------------------------------------------ */

public class GameImageRecorder {

    static public void main(String[] args) {

        // check command line arguments
        if (args.length != 1) {
            System.err.println("USAGE: java GameImageRecorder <directory>");
            System.exit(1);
        }
        if (!(new File(args[0])).isDirectory()) {
            System.err.println("ERROR: directory " + args[0] + " does not exist");
            System.exit(1);
        }

        GameImageRecorder recorder = new GameImageRecorder();
        // connect to game proxy
        Proxy proxy = null;
        try {
            proxy = new Proxy(9000) {
                @Override
                public void onOpen() {
                    System.out.println("Connected to game proxy");
                }

                @Override
                public void onClose() {
                    System.out.println("Disconnected from game proxy");
                }
                };
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        proxy.start();
        System.out.println("Waiting for proxy to connect");
        proxy.waitForClients(1);

        // enter game loop
        int frameCount = 0;
        BufferedImage screenshot = null;
        while (true) {
            // capture screenshot
            byte[] imageBytes = proxy.send(new ProxyScreenshotMessage());
            BufferedImage image = null;
            try {
                image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            } catch (IOException e) {
                e.printStackTrace();
            }

            // write image to disk
            if ((screenshot == null) ||
                (VisionUtils.numPixelsDifferent(screenshot, image) > 2048)) {
                final String imgFilename = String.format(args[0] + File.separator + "img%04d.png", frameCount);
                System.out.println("saving image to " + imgFilename);
                try {
                    ImageIO.write(image, "png", new File(imgFilename));
                } catch (IOException e) {
                    System.err.println("failed to save image " + imgFilename);
                    e.printStackTrace();
                }

                // update frame count
                screenshot = image;
                frameCount += 1;
            }

            // sleep for a while
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) { }
        }
    }
}
