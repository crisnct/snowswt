package org.herbshouse.gui.imageBuilder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.herbshouse.gui.GuiUtils;
import org.herbshouse.gui.SWTResourceManager;
import org.herbshouse.logic.AbstractMovableObject;
import org.herbshouse.logic.Generator;

class CountdownDrawer {

  public void draw(
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

}
