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

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;
import Jama.Matrix;

import ab.utils.*;
import ab.vision.VisionUtils;

/* Vision ----------------------------------------------------------------- */

public class Vision {

	private int _nHeight; // height of the scene
	private int _nWidth; // width of the scene
	private int _scene[][]; // quantized scene colours
	private int _nSegments; // number of segments
	private int _segments[][]; // connected components (0 to _nSegments)
	private int _colours[]; // colour for each segment
	private Rectangle _boxes[]; // bounding box for each segment
	private int _regionThreshold = 10; // minimal pixels in a region

	// create a vision object for processing a given screenshot
	public Vision(BufferedImage screenshot) {
		processScreenShot(screenshot);
	}

	// find slingshot in the current scene
	/*
	 * public Rectangle findSlingshot() {
	 * 
	 * // sling shot consists of a large region of colour 345 and smaller //
	 * regions of colours {64, 418} for (int n = 0; n < _nSegments; n++) { if
	 * (_colours[n] != 345) continue; // if (_boxes[n].width * _boxes[n].height
	 * < 32) continue;
	 * 
	 * Rectangle bounds = VisionUtils.dialateRectangle(_boxes[n],
	 * _boxes[n].width / 2 + 1, _boxes[n].height + 1); Rectangle obj =
	 * _boxes[n];
	 * 
	 * // look for overlapping bounding boxes of secondary colours in the //
	 * sling // TODO: reset once added secondary colour for (int m = 1; m <
	 * _nSegments; m++) { if (m == n) continue; if ((_colours[m] != 345))
	 * continue; // && (_colours[m] != 64) && (_colours[m] != 418)) // continue;
	 * final Rectangle bounds2 = VisionUtils.dialateRectangle( _boxes[m],
	 * _boxes[m].width / 2 + 1, _boxes[m].height + 1); if
	 * (bounds.intersects(bounds2)) { bounds.add(bounds2); obj.add(_boxes[m]); }
	 * }
	 * 
	 * // check aspect ratio if (obj.width > obj.height) continue;
	 * 
	 * // check dominant and secondary colours obj =
	 * VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight); int[] hist =
	 * histogram(obj); if ((hist[345] > Math.max(32, 0.1 * obj.width *
	 * obj.height)) && (hist[64] != 0)) { return obj; } }
	 * 
	 * return null; }
	 */
	public Rectangle findSlingshot() {
		Rectangle obj;

		// test for slingshot (mainly 345)
		int nPixel = _nWidth * _nHeight;
		Boolean ignorePixel[][] = new Boolean[_nHeight][_nWidth];

		for (int i = 0; i < _nHeight; i++) {
			for (int j = 0; j < _nWidth; j++) {
				ignorePixel[i][j] = false;
			}
		}

		for (int i = 0; i < _nHeight; i++) {
			for (int j = 0; j < _nWidth; j++) {
				if ((_scene[i][j] != 345) || ignorePixel[i][j])
					continue;
				obj = new Rectangle(j, i, 0, 0);
				LinkedList<Point> l = new LinkedList<Point>();

				LinkedList<Point> pointsinRec = new LinkedList<Point>();

				l.add(new Point(j, i));
				ignorePixel[i][j] = true;
				while (true) {
					if (l.isEmpty())
						break;
					Point p = l.pop();
					// check if the colours of the adjacent points of p is
					// belong to slingshot
					if (p.y < _nHeight - 1)
						if ((_scene[p.y + 1][p.x] == 345
								|| _scene[p.y + 1][p.x] == 418
								|| _scene[p.y + 1][p.x] == 273
								|| _scene[p.y + 1][p.x] == 281
								|| _scene[p.y + 1][p.x] == 209
								|| _scene[p.y + 1][p.x] == 346
								|| _scene[p.y + 1][p.x] == 354
								|| _scene[p.y + 1][p.x] == 282 || _scene[p.y + 1][p.x] == 351)
								&& !ignorePixel[p.y + 1][p.x]) {
							l.add(new Point(p.x, p.y + 1));
							obj.add(p.x, p.y + 1);
							pointsinRec.add(new Point(p.x, p.y + 1));
						}
					if (p.x < _nWidth - 1)
						if ((_scene[p.y][p.x + 1] == 345
								|| _scene[p.y][p.x + 1] == 418
								|| _scene[p.y][p.x + 1] == 346
								|| _scene[p.y][p.x + 1] == 354
								|| _scene[p.y][p.x + 1] == 273
								|| _scene[p.y][p.x + 1] == 281
								|| _scene[p.y][p.x + 1] == 209
								|| _scene[p.y][p.x + 1] == 282 || _scene[p.y + 1][p.x] == 351)
								&& !ignorePixel[p.y][p.x + 1]) {
							l.add(new Point(p.x + 1, p.y));
							obj.add(p.x + 1, p.y);
							pointsinRec.add(new Point(p.x, p.y + 1));
						}

					if (p.y > 0)
						if ((_scene[p.y - 1][p.x] == 345
								|| _scene[p.y - 1][p.x] == 418
								|| _scene[p.y - 1][p.x] == 346
								|| _scene[p.y - 1][p.x] == 354
								|| _scene[p.y - 1][p.x] == 273
								|| _scene[p.y - 1][p.x] == 281
								|| _scene[p.y - 1][p.x] == 209
								|| _scene[p.y - 1][p.x] == 282 || _scene[p.y + 1][p.x] == 351)
								&& !ignorePixel[p.y - 1][p.x]) {
							l.add(new Point(p.x, p.y - 1));
							obj.add(p.x, p.y - 1);
							pointsinRec.add(new Point(p.x, p.y + 1));
						}

					if (p.x > 0)
						if ((_scene[p.y][p.x - 1] == 345
								|| _scene[p.y][p.x - 1] == 418
								|| _scene[p.y][p.x - 1] == 346
								|| _scene[p.y][p.x - 1] == 354
								|| _scene[p.y][p.x - 1] == 273
								|| _scene[p.y][p.x - 1] == 281
								|| _scene[p.y][p.x - 1] == 209
								|| _scene[p.y][p.x - 1] == 282 || _scene[p.y + 1][p.x] == 351)
								&& !ignorePixel[p.y][p.x - 1]) {
							l.add(new Point(p.x - 1, p.y));
							obj.add(p.x - 1, p.y);
							pointsinRec.add(new Point(p.x, p.y + 1));
						}

					if (p.y < _nHeight - 1)
						ignorePixel[p.y + 1][p.x] = true;
					if (p.x < _nWidth - 1)
						ignorePixel[p.y][p.x + 1] = true;
					if (p.y > 0)
						ignorePixel[p.y - 1][p.x] = true;
					if (p.x > 0)
						ignorePixel[p.y][p.x - 1] = true;

				}
				int[] hist = histogram(obj);

				//abandon shelf underneath
				if (/*hist[511] >= obj.height * obj.width / 4 && */obj.height > 10) {
					Rectangle col = new Rectangle(obj.x, obj.y, 1, obj.height);
					int[] histCol = histogram(col);
					int ColColour = histCol[345] + histCol[418]
							+ histCol[346] + histCol[354] + histCol[273]
							+ histCol[281] + histCol[209] + histCol[280]
							+ histCol[351];
					
					if(_scene[obj.y][obj.x] == 511 || _scene[obj.y][obj.x] == 447){
						for(int m = obj.y; m < obj.y + obj.height; m++){
							if(_scene[m][obj.x] == 345
									|| _scene[m][obj.x] == 418
									|| _scene[m][obj.x] == 346
									|| _scene[m][obj.x] == 354
									|| _scene[m][obj.x] == 273
									|| _scene[m][obj.x] == 281
									|| _scene[m][obj.x] == 209
									|| _scene[m][obj.x] == 282 || _scene[m][obj.x] == 351){
								obj.setSize(obj.width, m-obj.y);
								break;
							}
						}
					}
					
					while (histCol[511] >= obj.height * 0.8) {						
						obj.setBounds(obj.x + 1, obj.y, obj.width - 1,
								obj.height);
						col = new Rectangle(obj.x + 1, obj.y, 1, obj.height);
						histCol = histogram(col);
					}

					col = new Rectangle(obj.x + obj.width, obj.y, 1, obj.height);
					histCol = histogram(col);
					while (histCol[511] >= obj.height * 0.8 && obj.height > 10) {
						obj.setSize(obj.width - 1, obj.height);
						col = new Rectangle(obj.x + obj.width, obj.y, 1,
								obj.height);
						histCol = histogram(col);
					}
				}

				if (obj.width > obj.height)
					continue;

				if ((hist[345] > Math.max(32, 0.1 * obj.width * obj.height))
						&& (hist[64] != 0)) {
					obj.add(new Rectangle(obj.x - obj.width / 10, obj.y
							- obj.height / 3, obj.width / 10 * 12,
							obj.height / 3 * 4));
					return obj;
				}
			}
		}
		return null;
	}

	// find pigs in the current scene
	public List<Rectangle> findPigs() {
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		// find candidates
		Boolean ignore[] = new Boolean[_nSegments];
		Arrays.fill(ignore, false);

		for (int n = 0; n < _nSegments; n++) {
			if ((_colours[n] != 376) || ignore[n])
				continue;

			// dilate bounding box of colour 376
			Rectangle bounds = VisionUtils.dialateRectangle(_boxes[n],
					_boxes[n].width / 2 + 1, _boxes[n].height / 2 + 1);
			Rectangle obj = _boxes[n];

			// look for overlapping bounding boxes of colour 376
			for (int m = n + 1; m < _nSegments; m++) {
				if (_colours[m] != 376)
					continue;
				final Rectangle bounds2 = VisionUtils.dialateRectangle(
						_boxes[m], _boxes[m].width / 2 + 1,
						_boxes[m].height / 2 + 1);
				if (bounds.intersects(bounds2)) {
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			// look for overlapping bounding boxes of colour 250
			Boolean bValidObject = false;
			for (int m = 0; m < _nSegments; m++) {
				if (_colours[m] != 250)
					continue;
				if (bounds.intersects(_boxes[m])) {
					bValidObject = true;
					break;
				}
			}

			// add object if valid
			if (bValidObject) {
				obj = VisionUtils.dialateRectangle(obj, obj.width / 2 + 1,
						obj.height / 2 + 1);
				obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);
				objects.add(obj);
			}
		}

		return objects;
	}

	// find birds in the current scene
	public List<Rectangle> findRedBirds() {
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		// test for red birds (385, 488, 501)
		Boolean ignore[] = new Boolean[_nSegments];
		Arrays.fill(ignore, false);

		for (int n = 0; n < _nSegments; n++) {
			if ((_colours[n] != 385) || ignore[n])
				continue;

			// dilate bounding box around colour 385
			Rectangle bounds = VisionUtils.dialateRectangle(_boxes[n], 1,
					_boxes[n].height / 2 + 1);
			Rectangle obj = _boxes[n];

			// look for overlapping bounding boxes of colour 385
			for (int m = n + 1; m < _nSegments; m++) {
				if (_colours[m] != 385)
					continue;
				final Rectangle bounds2 = VisionUtils.dialateRectangle(
						_boxes[m], 1, _boxes[m].height / 2 + 1);
				if (bounds.intersects(bounds2)) {
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			// look for overlapping bounding boxes of colours 488 and 501
			Boolean bValidObject = false;
			for (int m = 0; m < _nSegments; m++) {
				if ((_colours[m] != 488) && (_colours[m] != 501))
					continue;
				if (bounds.intersects(_boxes[m])) {
					obj.add(_boxes[m]);
					bValidObject = true;
				}
			}

			if (bValidObject) {
				obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);
				objects.add(obj);
			}
		}

		return objects;
	}

	public List<Rectangle> findBlueBirds() {
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		// test for blue birds (238)
		Boolean ignore[] = new Boolean[_nSegments];
		Arrays.fill(ignore, false);

		for (int n = 0; n < _nSegments; n++) {
			if ((_colours[n] != 238) || ignore[n])
				continue;

			// dilate bounding box around colour 238
			Rectangle bounds = VisionUtils.dialateRectangle(_boxes[n], 1,
					_boxes[n].height / 2 + 1);
			Rectangle obj = _boxes[n];

			// look for overlapping bounding boxes of colours 238, 165, 280,
			// 344, 488, 416
			for (int m = n + 1; m < _nSegments; m++) {
				if ((_colours[m] != 238) && (_colours[m] != 165)
						&& (_colours[m] != 280) && (_colours[m] != 344)
						&& (_colours[m] != 488) && (_colours[m] != 416))
					continue;
				final Rectangle bounds2 = VisionUtils.dialateRectangle(
						_boxes[m], 2, _boxes[m].height / 2 + 1);
				if (bounds.intersects(bounds2)) {
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			for (int m = n + 1; m < _nSegments; m++) {
				if (_colours[m] != 238)
					continue;
				final Rectangle bounds2 = VisionUtils.dialateRectangle(
						_boxes[m], 2, _boxes[m].height / 2 + 1);
				if (bounds.intersects(bounds2)) {
					ignore[m] = true;
				}
			}

			// look for overlapping bounding boxes of colours 488
			Boolean bValidObject = false;
			for (int m = 0; m < _nSegments; m++) {
				if (_colours[m] != 488)
					continue;
				if (bounds.intersects(_boxes[m])) {
					obj.add(_boxes[m]);
					bValidObject = true;
				}
			}

			if (bValidObject && (obj.width > 3)) {
				obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);
				objects.add(obj);
			}
		}

		return objects;
	}

	public List<Rectangle> findYellowBirds() {
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		// test for blue birds (497)
		Boolean ignore[] = new Boolean[_nSegments];
		Arrays.fill(ignore, false);

		for (int n = 0; n < _nSegments; n++) {
			if ((_colours[n] != 497) || ignore[n])
				continue;

			// dilate bounding box around colour 497
			Rectangle bounds = VisionUtils.dialateRectangle(_boxes[n], 2, 2);
			Rectangle obj = _boxes[n];

			// look for overlapping bounding boxes of colours 497
			for (int m = n + 1; m < _nSegments; m++) {
				if (_colours[m] != 497)
					continue;
				final Rectangle bounds2 = VisionUtils.dialateRectangle(
						_boxes[m], 2, 2);
				if (bounds.intersects(bounds2)) {
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			// confirm secondary colours 288
			obj = VisionUtils.dialateRectangle(obj, 2, 2);
			obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);
			int[] hist = histogram(obj);
			if (hist[288] > 0) {
				objects.add(obj);
			}
		}

		return objects;
	}

	public List<Rectangle> findStones() {
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		// test for stone (mainly 365)
		int nPixel = _nWidth * _nHeight;
		Boolean ignorePixel[][] = new Boolean[_nHeight][_nWidth];

		for (int i = 0; i < _nHeight; i++) {
			for (int j = 0; j < _nWidth; j++) {
				ignorePixel[i][j] = false;
			}
		}

		for (int i = 0; i < _nHeight; i++) {
			for (int j = 0; j < _nWidth; j++) {
				if ((_scene[i][j] != 365) || ignorePixel[i][j])
					continue;
				Rectangle obj = new Rectangle(j, i, 0, 0);
				LinkedList<Point> l = new LinkedList<Point>();
				l.add(new Point(j, i));
				ignorePixel[i][j] = true;
				while (true) {
					if (l.isEmpty())
						break;
					Point p = l.pop();
					// check if the colours of the adjacent points of p is
					// belong to stone
					if (p.y < _nHeight - 1)
						if ((_scene[p.y + 1][p.x] == 365
						/* || _scene[p.y + 1][p.x] == 292 */)
								&& !ignorePixel[p.y + 1][p.x]) {
							l.add(new Point(p.x, p.y + 1));
							obj.add(p.x, p.y + 1);
						}
					if (p.x < _nWidth - 1)
						if ((_scene[p.y][p.x + 1] == 365
						/* || _scene[p.y][p.x + 1] == 292 */)
								&& !ignorePixel[p.y][p.x + 1]) {
							l.add(new Point(p.x + 1, p.y));
							obj.add(p.x + 1, p.y);
						}

					if (p.y > 0)
						if ((_scene[p.y - 1][p.x] == 365
						/* || _scene[p.y - 1][p.x] == 292 */)
								&& !ignorePixel[p.y - 1][p.x]) {
							l.add(new Point(p.x, p.y - 1));
							obj.add(p.x, p.y - 1);
						}

					if (p.x > 0)
						if ((_scene[p.y][p.x - 1] == 365
						/* || _scene[p.y][p.x - 1] == 292 */)
								&& !ignorePixel[p.y][p.x - 1]) {
							l.add(new Point(p.x - 1, p.y));
							obj.add(p.x - 1, p.y);
						}

					if (p.y < _nHeight - 1)
						ignorePixel[p.y + 1][p.x] = true;
					if (p.x < _nWidth - 1)
						ignorePixel[p.y][p.x + 1] = true;
					if (p.y > 0)
						ignorePixel[p.y - 1][p.x] = true;
					if (p.x > 0)
						ignorePixel[p.y][p.x - 1] = true;

				}
				if (obj.width * obj.height > _regionThreshold
						&& !(new Rectangle(0, 0, 190, 55).contains(obj)))
					objects.add(obj);
			}
		}
		return objects;
	}

	public List<Rectangle> findIce() {
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		// test for ice (mainly 311)
		int nPixel = _nWidth * _nHeight;
		Boolean ignorePixel[][] = new Boolean[_nHeight][_nWidth];

		for (int i = 0; i < _nHeight; i++) {
			for (int j = 0; j < _nWidth; j++) {
				ignorePixel[i][j] = false;
			}

		}

		for (int i = 0; i < _nHeight; i++) {
			for (int j = 0; j < _nWidth; j++) {
				if ((_scene[i][j] != 311) || ignorePixel[i][j])
					continue;
				Rectangle obj = new Rectangle(j, i, 0, 0);
				LinkedList<Point> l = new LinkedList<Point>();
				l.add(new Point(j, i));
				ignorePixel[i][j] = true;
				while (true) {
					if (l.isEmpty())
						break;
					Point p = l.pop();
					// check if the colours of the adjacent points of p is
					// belong to ice
					if (p.y < _nHeight - 1)
						if ((_scene[p.y + 1][p.x] == 311
								|| _scene[p.y + 1][p.x] == 247 || _scene[p.y + 1][p.x] == 183)
								&& !ignorePixel[p.y + 1][p.x]) {
							l.add(new Point(p.x, p.y + 1));
							obj.add(p.x, p.y + 1);
						}
					if (p.x < _nWidth - 1)
						if ((_scene[p.y][p.x + 1] == 311
								|| _scene[p.y][p.x + 1] == 247 || _scene[p.y][p.x + 1] == 183)
								&& !ignorePixel[p.y][p.x + 1]) {
							l.add(new Point(p.x + 1, p.y));
							obj.add(p.x + 1, p.y);
						}
					if (p.y > 0)
						if ((_scene[p.y - 1][p.x] == 311
								|| _scene[p.y - 1][p.x] == 247 || _scene[p.y - 1][p.x] == 183)
								&& !ignorePixel[p.y - 1][p.x]) {
							l.add(new Point(p.x, p.y - 1));
							obj.add(p.x, p.y - 1);
						}
					if (p.x > 0)
						if ((_scene[p.y][p.x - 1] == 311
								|| _scene[p.y][p.x - 1] == 247 || _scene[p.y][p.x - 1] == 183)
								&& !ignorePixel[p.y][p.x - 1]) {
							l.add(new Point(p.x - 1, p.y));
							obj.add(p.x - 1, p.y);
						}

					if (p.y < _nHeight - 1)
						ignorePixel[p.y + 1][p.x] = true;
					if (p.x < _nWidth - 1)
						ignorePixel[p.y][p.x + 1] = true;
					if (p.y > 0)
						ignorePixel[p.y - 1][p.x] = true;
					if (p.x > 0)
						ignorePixel[p.y][p.x - 1] = true;

				}
				if (obj.width * obj.height > _regionThreshold
						&& !(new Rectangle(0, 0, 190, 55).contains(obj)))
					objects.add(obj);
			}
		}
		return objects;
	}

	public List<Rectangle> findWood() {
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		// test for wood (mainly 481)
		int nPixel = _nWidth * _nHeight;
		// Boolean ignore[] = new Boolean[_nSegments];
		Boolean ignorePixel[][] = new Boolean[_nHeight][_nWidth];
		// for (int n = 0; n < _nSegments; n++) {
		// ignore[n] = false;
		// }

		for (int i = 0; i < _nHeight; i++) {
			for (int j = 0; j < _nWidth; j++) {
				ignorePixel[i][j] = false;
			}

		}

		for (int i = 0; i < _nHeight; i++) {
			for (int j = 0; j < _nWidth; j++) {
				if ((_scene[i][j] != 481) || ignorePixel[i][j])
					continue;
				Rectangle obj = new Rectangle(j, i, 0, 0);
				LinkedList<Point> l = new LinkedList<Point>();
				l.add(new Point(j, i));
				ignorePixel[i][j] = true;
				while (true) {
					if (l.isEmpty())
						break;
					Point p = l.pop();
					// check if the colours of the adjacent points of p is
					// belong to wood
					if (p.y < _nHeight - 1)
						if ((_scene[p.y + 1][p.x] == 481
								|| _scene[p.y + 1][p.x] == 408 || _scene[p.y + 1][p.x] == 417)
								&& !ignorePixel[p.y + 1][p.x]) {
							l.add(new Point(p.x, p.y + 1));
							obj.add(p.x, p.y + 1);
						}
					if (p.x < _nWidth - 1)
						if ((_scene[p.y][p.x + 1] == 481
								|| _scene[p.y][p.x + 1] == 408 || _scene[p.y][p.x + 1] == 417)
								&& !ignorePixel[p.y][p.x + 1]) {
							l.add(new Point(p.x + 1, p.y));
							obj.add(p.x + 1, p.y);
						}
					if (p.y > 0)
						if ((_scene[p.y - 1][p.x] == 481
								|| _scene[p.y - 1][p.x] == 408 || _scene[p.y - 1][p.x] == 417)
								&& !ignorePixel[p.y - 1][p.x]) {
							l.add(new Point(p.x, p.y - 1));
							obj.add(p.x, p.y - 1);
						}
					if (p.x > 0)
						if ((_scene[p.y][p.x - 1] == 481
								|| _scene[p.y][p.x - 1] == 408 || _scene[p.y][p.x - 1] == 417)
								&& !ignorePixel[p.y][p.x - 1]) {
							l.add(new Point(p.x - 1, p.y));
							obj.add(p.x - 1, p.y);
						}

					if (p.y < _nHeight - 1)
						ignorePixel[p.y + 1][p.x] = true;
					if (p.x < _nWidth - 1)
						ignorePixel[p.y][p.x + 1] = true;
					if (p.y > 0)
						ignorePixel[p.y - 1][p.x] = true;
					if (p.x > 0)
						ignorePixel[p.y][p.x - 1] = true;

				}
				if (obj.width * obj.height > _regionThreshold
						&& !(new Rectangle(0, 0, 190, 55).contains(obj)))
					objects.add(obj);
			}
		}

		return objects;
	}

	// find trajectory points
	public ArrayList<Point> findTrajPoints() {
		ArrayList<Point> objects = new ArrayList<Point>();
		ArrayList<Point> objectsRemovedNoise;

		// test for trajectory points
		int nPixel = _nWidth * _nHeight;
		Boolean ignorePixel[][] = new Boolean[_nHeight][_nWidth];

		for (int i = 0; i < _nHeight; i++) {
			for (int j = 0; j < _nWidth; j++) {
				ignorePixel[i][j] = false;
			}

		}

		for (int i = 0; i < _nHeight; i++) {
			for (int j = 0; j < _nWidth; j++) {
				if ((_scene[i][j] != 365 && _scene[i][j] != 366 && _scene[i][j] != 438)
						|| ignorePixel[i][j])
					continue;
				Rectangle obj = new Rectangle(j, i, 0, 0);
				LinkedList<Point> l = new LinkedList<Point>();
				l.add(new Point(j, i));
				ignorePixel[i][j] = true;
				while (true) {
					if (l.isEmpty())
						break;
					Point p = l.pop();
					// check if the colours of the adjacent points of p is
					// belong to traj Points
					if (p.y < _nHeight - 1 && p.x < _nWidth - 1 && p.y > 0
							&& p.x > 0) {
						if ((_scene[p.y + 1][p.x] == 365
								|| _scene[p.y + 1][p.x] == 366 || _scene[p.y + 1][p.x] == 438)
								&& !ignorePixel[p.y + 1][p.x]) {
							l.add(new Point(p.x, p.y + 1));
							obj.add(p.x, p.y + 1);
						}

						if ((_scene[p.y][p.x + 1] == 365
								|| _scene[p.y][p.x + 1] == 366 || _scene[p.y][p.x + 1] == 438)
								&& !ignorePixel[p.y][p.x + 1]) {
							l.add(new Point(p.x + 1, p.y));
							obj.add(p.x + 1, p.y);
						}

						if ((_scene[p.y - 1][p.x] == 365
								|| _scene[p.y - 1][p.x] == 366 || _scene[p.y - 1][p.x] == 438)
								&& !ignorePixel[p.y - 1][p.x]) {
							l.add(new Point(p.x, p.y - 1));
							obj.add(p.x, p.y - 1);
						}

						if ((_scene[p.y][p.x - 1] == 365
								|| _scene[p.y][p.x - 1] == 366 || _scene[p.y][p.x - 1] == 438)
								&& !ignorePixel[p.y][p.x - 1]) {
							l.add(new Point(p.x - 1, p.y));
							obj.add(p.x - 1, p.y);
						}

						if ((_scene[p.y - 1][p.x - 1] == 365
								|| _scene[p.y - 1][p.x - 1] == 366 || _scene[p.y - 1][p.x - 1] == 438)
								&& !ignorePixel[p.y - 1][p.x - 1]) {
							l.add(new Point(p.x - 1, p.y - 1));
							obj.add(p.x - 1, p.y - 1);
						}

						if ((_scene[p.y - 1][p.x + 1] == 365
								|| _scene[p.y - 1][p.x + 1] == 366 || _scene[p.y - 1][p.x + 1] == 438)
								&& !ignorePixel[p.y - 1][p.x + 1]) {
							l.add(new Point(p.x + 1, p.y - 1));
							obj.add(p.x + 1, p.y - 1);
						}

						if ((_scene[p.y + 1][p.x + 1] == 365
								|| _scene[p.y + 1][p.x + 1] == 366 || _scene[p.y + 1][p.x + 1] == 438)
								&& !ignorePixel[p.y + 1][p.x + 1]) {
							l.add(new Point(p.x + 1, p.y + 1));
							obj.add(p.x + 1, p.y + 1);
						}

						if ((_scene[p.y + 1][p.x - 1] == 365
								|| _scene[p.y + 1][p.x - 1] == 366 || _scene[p.y + 1][p.x - 1] == 438)
								&& !ignorePixel[p.y + 1][p.x - 1]) {
							l.add(new Point(p.x - 1, p.y + 1));
							obj.add(p.x - 1, p.y + 1);
						}

					}
					if (p.y < _nHeight - 1 && p.x < _nWidth - 1 && p.y > 0
							&& p.x > 0) {
						ignorePixel[p.y + 1][p.x] = true;
						ignorePixel[p.y][p.x + 1] = true;

						ignorePixel[p.y - 1][p.x] = true;
						ignorePixel[p.y][p.x - 1] = true;

						ignorePixel[p.y + 1][p.x + 1] = true;
						ignorePixel[p.y - 1][p.x + 1] = true;
						ignorePixel[p.y + 1][p.x - 1] = true;
						ignorePixel[p.y - 1][p.x - 1] = true;
					}
				}

				Rectangle menu = new Rectangle(0,0,205,60);
				if (obj.height * obj.width <= 25)
					objects.add(new Point((int)obj.getCenterX(),(int)obj.getCenterY()));
			}
		}

		objectsRemovedNoise = (ArrayList<Point>) objects.clone();

		// remove noise points
		Matrix W = fitParabola(objects);
		double maxError = 10;
		Rectangle menu = new Rectangle(0,0,205,60);
		
		for (Point o : objects) {
			if (Math.abs(W.get(0, 0) * Math.pow(o.x, 2)
					+ W.get(1, 0) * o.x + W.get(2, 0)
					- o.y) > maxError) {
				objectsRemovedNoise.remove(o);
			}
			
			if(menu.contains(o)){
				objectsRemovedNoise.remove(o);
			}
		}
        
		return objectsRemovedNoise;
	}

	/**
	 * fit parabola using maximum likelihood
	 * 
	 * @param objects
	 * @return parameter vector W = (w0,w1,w2)T , y = w0*x^2 + w1*x + w2
	 */
	public Matrix fitParabola(List<Point> objects) {
		int trainingSize = 60;
		double arrayPhiX[][] = new double[trainingSize][3]; // Training set
		double arrayY[][] = new double[trainingSize][1];

		Rectangle sling = this.findSlingshot();

		Matrix PhiX, Y;
		Matrix W = new Matrix(new double[] { 0, 0, 0 }, 3);
		int i = 0;
		for (Point p : objects) {
			if (sling == null) {
				if (Math.abs(p.x - _nWidth / 2) <= _nWidth / 6
						&& p.y <= _nHeight / 5 * 3
						&& i < trainingSize) {
					arrayPhiX[i][0] = Math.pow(p.x, 2);
					arrayPhiX[i][1] = p.x;
					arrayPhiX[i][2] = 1;
					arrayY[i][0] = p.y;
					i++;
				}
			} else {
				if (p.x >= sling.getCenterX() + sling.width * 2
						&& p.x <= sling.getCenterX() + _nWidth / 3
						&& p.y <= sling.getCenterY()
						&& i < trainingSize) {
					arrayPhiX[i][0] = Math.pow(p.x, 2);
					arrayPhiX[i][1] = p.x;
					arrayPhiX[i][2] = 1;
					arrayY[i][0] = p.y;
					i++;
				}
			}
		}

		PhiX = new Matrix(arrayPhiX);
		Y = new Matrix(arrayY);

		// Maximum likelihood
		try {
			W = PhiX.transpose().times(PhiX).inverse().times(PhiX.transpose())
					.times(Y);
		} catch (Exception e) {
			// if Matrix is singular
			// do nothing
		}
		return W;
	}

	// train parabola using gradient descent
	public Matrix trainParabola(ArrayList<Rectangle> objects) {

		double points[][] = new double[objects.size()][2];
		double alpha = 1e-10;
		int trainingSize = 100;

		double trainingSet[][] = new double[trainingSize][2];
		double SquareError;
		Matrix deltaError;

		int i = 0, j = 0;
		for (Rectangle p : objects) {
			points[i][0] = p.getCenterX();
			points[i][1] = p.getCenterY();
			if (Math.abs(p.getCenterX() - _nWidth / 2) <= _nWidth / 4
					&& Math.abs(p.getCenterY() - _nHeight / 2) <= _nHeight / 5
					&& j < trainingSize) {
				trainingSet[j][0] = points[i][0];
				trainingSet[j][1] = points[i][1];
				j++;
			}
			i++;
		}

		Matrix T = new Matrix(trainingSet);// possible traj points matrix
		Matrix W = new Matrix(new double[] { 0, 0, 0 }, 3);// parabola
															// parameters
		Matrix oldW;
		Matrix phiX;

		for (int x = -50; x < 50; x++) {
			if (x + 50 < trainingSize) {
				trainingSet[x + 50][0] = x;
				trainingSet[x + 50][1] = -x * x + 20 * x + 1;
			}
		}

		for (int it = 0; it < 50000; it++) {
			SquareError = 0.;
			for (int n = 0; n < trainingSize; n++) {
				if (trainingSet[n][0] > 0) {
					double xn = trainingSet[n][0];
					double yn = trainingSet[n][1];
					phiX = new Matrix(new double[] { Math.pow(xn, 2), xn, 1. },
							3);

					deltaError = phiX.times((yn - W.transpose().times(phiX)
							.get(0, 0)));
					oldW = W;

					W = W.plus(deltaError.times(alpha));
					SquareError += Math.pow(
							yn - phiX.transpose().times(W).get(0, 0), 2);

				}
			}
			if (it % 1000 == 0) {
				System.out.print(SquareError + "\n");
				W.print(1, 30);
			}
		}

		return W;

	}

	// find bounding boxes around an arbitrary colour code
	public List<Rectangle> findColour(int colourCode) {
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		for (int n = 0; n < _nSegments; n++) {
			if (_colours[n] == colourCode) {
				objects.add(_boxes[n]);
			}
		}

		return objects;
	}

	// query the colour at given pixel
	public Integer query(Point p) {
		if ((p.x >= _nWidth) || (p.y >= _nHeight)) {
			System.err.println("pixel (" + p.x + ", " + p.y
					+ ") is out of range");
			return null;
		}

		return _colours[_segments[p.y][p.x]];
	}

	// query colours within given bounding box
	public Set<Integer> query(Rectangle r) {
		Set<Integer> s = new HashSet<Integer>();
		for (int n = 0; n < _nSegments; n++) {
			if (r.contains(_boxes[n])) {
				s.add(_colours[n]);
			}
		}
		return s;
	}

	// compute a histogram of colours within a given bounding box
	public int[] histogram(Rectangle r) {
		int[] h = new int[512];
		Arrays.fill(h, 0);

		for (int y = r.y; y < r.y + r.height; y++) {
			if ((y < 0) || (y >= _nHeight))
				continue;
			for (int x = r.x; x < r.x + r.width; x++) {
				if ((x < 0) || (x >= _nWidth))
					continue;
				h[_colours[_segments[y][x]]] += 1;
			}
		}

		return h;
	}

	// perform preprocessing of a new screenshot
	private void processScreenShot(BufferedImage screenshot) {
		// extract width and height
		_nHeight = screenshot.getHeight();
		_nWidth = screenshot.getWidth();
		if ((_nHeight != 480) && (_nWidth != 840)) {
			System.err.println("ERROR: expecting 840-by-480 image");
			System.exit(1);
		}

		// quantize to 3-bit colour
		_scene = new int[_nHeight][_nWidth];
		for (int y = 0; y < _nHeight; y++) {
			for (int x = 0; x < _nWidth; x++) {
				final int colour = screenshot.getRGB(x, y);
				_scene[y][x] = ((colour & 0x00e00000) >> 15)
						| ((colour & 0x0000e000) >> 10)
						| ((colour & 0x000000e0) >> 5);
			}
		}

		// find connected components
		_segments = VisionUtils.findConnectedComponents(_scene);
		_nSegments = VisionUtils.countComponents(_segments);
		// System.out.println("...found " + _nSegments + " components");

		_colours = new int[_nSegments];
		for (int y = 0; y < _nHeight; y++) {
			for (int x = 0; x < _nWidth; x++) {
				_colours[_segments[y][x]] = _scene[y][x];
			}
		}

		// find bounding boxes and segment colours
		_boxes = VisionUtils.findBoundingBoxes(_segments);
	}
}
