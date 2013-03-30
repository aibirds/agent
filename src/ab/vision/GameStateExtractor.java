/*****************************************************************************
** ANGRYBIRDS AI AGENT FRAMEWORK
** Copyright (c) 2013,XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
**  Sahan Abeyasinghe, Jim Keys, Kar-Wai Lim, Zain Mubashir,  Andrew Wang, Peng Zhang
** All rights reserved.
**This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
**To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
*or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
*****************************************************************************/

package ab.vision;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import ab.utils.ShowDebuggingImage;

/* GameStateExtractor ----------------------------------------------------- */

public class GameStateExtractor {

    public enum GameState {
        MAIN_MENU, EPISODE_MENU, LEVEL_SELECTION, LOADING, PLAYING, WON, LOST, UNKNOWN
    }

    // images for determining game state
    private static BufferedImage _mainmenu = null;
    private static BufferedImage _episodemenu = null;
    private static BufferedImage _levelselection = null;
    private static BufferedImage _loading = null;
    private static BufferedImage _loading2 = null;
    private static BufferedImage _gamewon = null;
    private static BufferedImage _gamelost = null;

    private static ShowDebuggingImage _debug = null;

    private static class RectLeftOf implements java.util.Comparator<Rectangle>{
        public int compare(Rectangle rA,Rectangle rB){
            return (rA.x - rB.x);
        }
    }

    // create a game state extractor and load subimages
    public GameStateExtractor() {
        try {
            _mainmenu = ImageIO.read(getClass().getResource("resources/mainmenu.png"));
            _episodemenu = ImageIO.read(getClass().getResource("resources/episodemenu.png"));
            _levelselection = ImageIO.read(getClass().getResource("resources/levelselection.png"));
            _loading = ImageIO.read(getClass().getResource("resources/loading.png"));
            _loading2 = ImageIO.read(getClass().getResource("resources/loading2.png"));
            _gamewon = ImageIO.read(getClass().getResource("resources/gamewon.png"));
            _gamelost = ImageIO.read(getClass().getResource("resources/gamelost.png"));
        } catch (IOException e) {
            System.err.println("failed to load resources");
            e.printStackTrace();
        }        
    }

    public GameState getGameState(BufferedImage screenshot) {

        // pixel colour deviation threshold for valid detection
        final int avgColourThreshold = 5;

        // check for main menu or episode menu or level selection
        BufferedImage wnd = screenshot.getSubimage(636, 24, 192, 26);
        String imgHash = VisionUtils.imageDigest(wnd);

        int numBytes = 3 * wnd.getWidth() * wnd.getHeight();
        if (VisionUtils.imageDifference(wnd, _mainmenu) < numBytes * avgColourThreshold) {
            return GameState.MAIN_MENU;
        } else if (VisionUtils.imageDifference(wnd, _episodemenu) < numBytes * avgColourThreshold) {
            return GameState.EPISODE_MENU;
        } else if (VisionUtils.imageDifference(wnd, _levelselection) < numBytes * avgColourThreshold) {
            return GameState.LEVEL_SELECTION;
        } else if ((VisionUtils.imageDifference(wnd, _loading) < numBytes * avgColourThreshold) ||
            (VisionUtils.imageDifference(wnd, _loading2) < numBytes * avgColourThreshold)) {
            return GameState.LOADING;
        }
        // otherwise check for end game or playing
        wnd = screenshot.getSubimage(320, 58, 192, 26);
        numBytes = 3 * wnd.getWidth() * wnd.getHeight();
        if (VisionUtils.imageDifference(wnd, _gamewon) < numBytes * avgColourThreshold) {
            return GameState.WON;
        }

        wnd = screenshot.getSubimage(320, 112, 192, 26);
        numBytes = 3 * wnd.getWidth() * wnd.getHeight();
        if (VisionUtils.imageDifference(wnd, _gamelost) < numBytes * avgColourThreshold) {
            return GameState.LOST;
        }

        return GameState.PLAYING;
    }

    public int getScoreInGame(BufferedImage screenshot) {
        // crop score image
        BufferedImage scoreImage = screenshot.getSubimage(632, 21, 200, 32);

        // extract characters
        int mask[][] = new int[scoreImage.getHeight()][scoreImage.getWidth()];
        for (int y = 0; y < scoreImage.getHeight(); y++) {
            for (int x = 0; x < scoreImage.getWidth(); x++) {
                final int colour = scoreImage.getRGB(x, y);
                mask[y][x] = ((colour & 0x00ffffff) == 0x00ffffff) ? 1 : -1;
            }
        }
        scoreImage = VisionUtils.int2image(mask);
        mask = VisionUtils.findConnectedComponents(mask);
        Rectangle[] letters = VisionUtils.findBoundingBoxes(mask);
        Arrays.sort(letters, new RectLeftOf());

        // decode letters
        int score = 0;
        for (int i = 0; i < letters.length; i++) {
            if (letters[i].width < 2) continue;

            BufferedImage letterImage = scoreImage.getSubimage(letters[i].x, letters[i].y,
                letters[i].width, letters[i].height);
            final String letterHash = VisionUtils.imageDigest(letterImage);

            int value = 0;
            if (letterHash.equals("62d05c5ce368be507a096aa6b5c68aeb")) {
                value = 1;
            } else if (letterHash.equals("518b4a3878a75aad32e23da4781e4c14")) {
                value = 2;
            } else if (letterHash.equals("be2b93e09c0f94a7c93b1b9cc675b26d")) {
                value = 3;
            } else if (letterHash.equals("3171f145ff67389b22d50ade7a13b5f7")) {
                value = 4;
            } else if (letterHash.equals("96c7dc988a5ad5aa50c3958a0f7869f4")) {
                value = 5;
            } else if (letterHash.equals("049b9aa34adf05ff2cca8cd4057a4d6b")) {
                value = 6;
            } else if (letterHash.equals("897aca1b39d4e2f6bc58b658e8819191")) {
                value = 7;
            } else if (letterHash.equals("e66e8aca895a06c1c9200b1b6b781567")) {
                value = 8;
            } else if (letterHash.equals("41c3010757c2e707146aa5d136e72c7a")) {
                value = 9;
            }

            score = 10 * score + value;
            //System.out.println(i + " : " + letters[i] + " : " + letterHash + " : " + value);
        }

        /*
        VisionUtils.drawBoundingBoxes(scoreImage, letters, Color.BLUE);
        if (_debug == null) {
            _debug = new ShowDebuggingImage("score", scoreImage);
        } else {
            _debug.refresh(scoreImage);
        }
        */

        return score;
    }

    public int getScoreEndGame(BufferedImage screenshot) {
        // crop score image
        BufferedImage scoreImage = screenshot.getSubimage(360, 265, 115, 32);

        // extract characters
        int mask[][] = new int[scoreImage.getHeight()][scoreImage.getWidth()];
        for (int y = 0; y < scoreImage.getHeight(); y++) {
            for (int x = 0; x < scoreImage.getWidth(); x++) {
                final int colour = scoreImage.getRGB(x, y);
                //mask[y][x] = ((colour & 0x00ffffff) != 0x00000000) ? 1 : -1;
                mask[y][x] = (((colour & 0x00ff0000) >> 16) > 192) ? 1 : -1;
            }
        }
        scoreImage = VisionUtils.int2image(mask);
        mask = VisionUtils.findConnectedComponents(mask);
        Rectangle[] letters = VisionUtils.findBoundingBoxes(mask);
        Arrays.sort(letters, new RectLeftOf());

        // decode letters
        int score = 0;
        for (int i = 0; i < letters.length; i++) {
            if (letters[i].width < 2) continue;

            BufferedImage letterImage = scoreImage.getSubimage(letters[i].x, letters[i].y,
                letters[i].width, letters[i].height);

	    // TODO: need method for classifying digit
	    int value = 0;
            score = 10 * score + value;
        }

        /*
        VisionUtils.drawBoundingBoxes(scoreImage, letters, Color.BLUE);
        if (_debug == null) {
            _debug = new ShowDebuggingImage("score", scoreImage);
        } else {
            _debug.refresh(scoreImage);
        }
        */

        return score;
    }
}
