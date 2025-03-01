package org.herbshouse.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.herbshouse.SnowingApplication;
import org.herbshouse.logic.AbstractMovableObject;
import org.herbshouse.logic.Point2D;
import org.herbshouse.logic.Utils;
import org.herbshouse.logic.enemies.RedFace;
import org.herbshouse.logic.snow.Snowflake;

public final class GuiUtils {

  public static final RGB FREEZE_COLOR = new RGB(0, 255, 255);
  public static final Rectangle SCREEN_BOUNDS = Display.getDefault().getBounds();
  private static final int FONT_SIZE_LIFE_COUNTER = 15;
  private static final String FONT_NAME_LIFE_COUNTER = "Arial";

  private GuiUtils() {
  }

  public static RGB getPixelColor(ImageData imageData, int x, int y) {
    if (x < 0 || y < 0 || x >= imageData.width || y >= imageData.height) {
      return new RGB(0, 0, 0);
    }
    return imageData.palette.getRGB(imageData.getPixel(x, y));
  }

  public static void draw(GC gc, AbstractMovableObject movableObject) {
    draw(gc, movableObject, movableObject.getLocation());
  }

  public static void drawRedFace(GC gc, RedFace redFace) {
    draw(gc, redFace);
    drawEyes(gc, redFace);
    drawLifeCounter(gc, redFace);
  }

  private static void drawEyes(GC gc, RedFace redFace) {
    gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
    gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
    gc.setLineWidth(2);
    drawOval(gc, redFace.getLeftEyeLoc(), redFace.getEyesSize());
    drawOval(gc, redFace.getRightEyeLoc(), redFace.getEyesSize());
    drawFillOval(gc, redFace.getLeftPupilLoc(), redFace.getPupilSize(), redFace.getPupilSize());
    drawFillOval(gc, redFace.getRightPupilLoc(), redFace.getPupilSize(), redFace.getPupilSize());
  }

  private static void drawLifeCounter(GC gc, RedFace redFace) {
    if (redFace.getKissingGif() == null) {
      String life = String.valueOf(redFace.getLife());
      gc.setFont(SWTResourceManager.getFont(FONT_NAME_LIFE_COUNTER, FONT_SIZE_LIFE_COUNTER, SWT.NORMAL));
      int textLength = gc.textExtent(life).x;
      gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
      Point screenLoc = toScreenCoord(redFace.getLocation());
      gc.drawText(life, (int) (screenLoc.x - textLength / 2.0d), screenLoc.y, true);
    }
  }

  public static void drawOval(GC gc, Point2D loc, Point2D size) {
    Point screenLoc = toScreenCoord(loc);
    gc.drawOval((int) (screenLoc.x - size.x / 2), (int) (screenLoc.y - size.y / 2), (int) size.x,
        (int) size.y);
  }

  public static void drawFillOval(GC gc, Point2D loc, double sizeX, double sizeY) {
    Point screenLoc = toScreenCoord(loc);
    gc.fillOval((int) (screenLoc.x - sizeX / 2), (int) (screenLoc.y - sizeY / 2), (int) sizeX,
        (int) sizeY);
  }

  public static void draw(GC gc, AbstractMovableObject movableObject, Point2D loc) {
    Color color = SWTResourceManager.getColor(movableObject.getColor());
    gc.setBackground(color);
    gc.setForeground(color);
    int prevAlpha = gc.getAlpha();
    gc.setAlpha(movableObject.getAlpha());
    Point screenObjLoc = toScreenCoord(loc);
    int locX = screenObjLoc.x - movableObject.getSize() / 2;
    int locY = screenObjLoc.y - movableObject.getSize() / 2;

    if (movableObject instanceof Snowflake snowflake && snowflake.isFreezed()) {
      locY += movableObject.getSize() / 3;
    }
    gc.fillOval(locX, locY, movableObject.getSize(), movableObject.getSize());
    gc.setAlpha(prevAlpha);
  }

  public static void drawGradientRectangle(GC gc, Point2D loc, double width, double height) {
    Point screenObjLoc = toScreenCoord(loc);
    gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
    gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
    gc.fillGradientRectangle(screenObjLoc.x, screenObjLoc.y, (int) width, (int) height, true);
  }

  public static Point toScreenCoord(Point2D loc) {
    return toScreenCoord(loc.x, loc.y);
  }

  public static Point toScreenCoord(double locX, double locY) {
    return new Point((int) locX, (int) (SCREEN_BOUNDS.height - locY));
  }

  public static Point2D toWorldCoord(int locX, double locY) {
    return new Point2D(locX, SCREEN_BOUNDS.height - locY);
  }

  public static Point2D toWorldCoord(Point loc) {
    return toWorldCoord(loc.x, loc.y);
  }

  public static void drawSnowflakeAsMercedes(GC gc, AbstractMovableObject snowflake) {
    Point screenLoc = toScreenCoord(snowflake.getLocation());
    gc.drawImage(SnowingApplication.mbImageSmall,
        screenLoc.x - SnowingApplication.MB_ICON_SIZE / 2,
        screenLoc.y - SnowingApplication.MB_ICON_SIZE / 2);
  }

  public static boolean equalsColors(RGB color1, RGB color2, int eps) {
    return Math.abs(color1.red - color2.red) <= eps
        && Math.abs(color1.green - color2.green) <= eps
        && Math.abs(color1.blue - color2.blue) <= eps;
  }

  public static void moveMouseAndClick(int locX, int locY, int origLocX, int origLocY) {
    postMouseEvent(SWT.MouseMove, locX, locY);
    postMouseEvent(SWT.MouseDown, locX, locY, 1);
    postMouseEvent(SWT.MouseUp, locX, locY, 1);
    postMouseEvent(SWT.MouseMove, origLocX, origLocY);
  }

  private static void postMouseEvent(int eventType, int x, int y) {
    Event event = new Event();
    event.type = eventType;
    event.x = x;
    event.y = y;
    event.doit = true;
    Display.getDefault().post(event);
  }

  private static void postMouseEvent(int eventType, int x, int y, int button) {
    Event event = new Event();
    event.type = eventType;
    event.x = x;
    event.y = y;
    event.button = button;
    event.doit = true;
    Display.getDefault().post(event);
  }

  public static void postKeyEvent(int keyCode) {
    Event event = new Event();
    event.type = SWT.KeyDown;
    event.keyCode = keyCode;
    event.character = (char) keyCode;
    event.doit = true;
    Display.getDefault().post(event);
    Utils.sleep(1);
    event = new Event();
    event.type = SWT.KeyUp;
    event.character = (char) keyCode;
    event.keyCode = keyCode;
    event.doit = true;
    Display.getDefault().post(event);
  }

  public static int[] toScreenCoord(double[] circlePoints) {
    int[] result = new int[circlePoints.length];
    for (int i = 0; i < circlePoints.length; i += 2) {
      Point screenLoc = toScreenCoord(new Point2D(circlePoints[i], circlePoints[i + 1]));
      result[i] = screenLoc.x;
      result[i + 1] = screenLoc.y;
    }
    return result;
  }

  public static void drawLine(GC gc, Point2D start, Point2D end, double thickness) {
    Point startScreenLoc = toScreenCoord(start);
    Point endScreenLoc = toScreenCoord(end);
    gc.setLineWidth((int) thickness);
    gc.drawLine(startScreenLoc.x, startScreenLoc.y, endScreenLoc.x, endScreenLoc.y);
    gc.setLineWidth(1);
  }

  public static void drawDot(GC gc, Point2D dot) {
    Point screenLoc = toScreenCoord(dot);
    gc.drawPoint(screenLoc.x, screenLoc.y);
  }

}