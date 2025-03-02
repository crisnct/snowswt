package org.herbshouse.gui.imageBuilder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.herbshouse.gui.GuiUtils;
import org.herbshouse.gui.SWTResourceManager;
import org.herbshouse.logic.AbstractMovableObject;
import org.herbshouse.logic.Generator;

class TextDrawer {

  private double alphaSkipDemo = 255;

  private double alphaSkipDemoDir = -10;

  public void drawCenteredText(GC gc, String text, Font font, int color) {
    gc.setForeground(Display.getDefault().getSystemColor(color));
    gc.setFont(font);
    Point textSize = gc.stringExtent(text);
    gc.drawText(text, (GuiUtils.SCREEN_BOUNDS.width - textSize.x) / 2,
        (GuiUtils.SCREEN_BOUNDS.height - textSize.y) / 2, true);
  }

  public void drawCountDown(
      GC gc,
      Generator<? extends AbstractMovableObject> generator,
      String textFromScreen
  ) {
    if (gc == null) {
      throw new IllegalArgumentException("Unproper usage of SwtImageBuilder");
    }
    //Draw countdown
    if (generator.getCountdown() >= 0) {
      if (generator.getCountdown() >= 4) {
        gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
      } else {
        gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
      }
      gc.setFont(SWTResourceManager.getFont("Arial", 25, SWT.BOLD));
      String countdown = String.valueOf(generator.getCountdown());
      Point countdownSize = gc.stringExtent(countdown);
      Point textSize = gc.stringExtent(textFromScreen);
      gc.drawText(countdown, (GuiUtils.SCREEN_BOUNDS.width - countdownSize.x) / 2,
          GuiUtils.SCREEN_BOUNDS.height / 2 + textSize.y, true);
    }
  }

  public void drawSkipDemo(GC gc) {
    String text = "- Push S to skip demo mode -";
    gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
    gc.setFont(SWTResourceManager.getFont("Arial", 12, SWT.BOLD));
    Point textSize = gc.stringExtent(text);

    alphaSkipDemo += alphaSkipDemoDir;
    if (alphaSkipDemo <= 30 || alphaSkipDemo >= 255) {
      alphaSkipDemoDir = -alphaSkipDemoDir;
    }
    gc.setAlpha((int) alphaSkipDemo);
    gc.drawText(text, (GuiUtils.SCREEN_BOUNDS.width - textSize.x) / 2,
        GuiUtils.SCREEN_BOUNDS.height - 30, true);
    gc.setAlpha(255);
  }

}