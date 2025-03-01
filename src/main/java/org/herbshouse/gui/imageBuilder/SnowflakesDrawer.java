package org.herbshouse.gui.imageBuilder;

import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.herbshouse.controller.FlagsConfiguration;
import org.herbshouse.controller.MainController;
import org.herbshouse.gui.GuiUtils;
import org.herbshouse.logic.Generator;
import org.herbshouse.logic.Point2D;
import org.herbshouse.logic.Utils;
import org.herbshouse.logic.snow.Snowflake;

class SnowflakesDrawer {

  private final MainController controller;

  public SnowflakesDrawer(MainController controller) {
    this.controller = controller;
  }

  public void draw(GC gc, Generator<Snowflake> generator) {
    if (gc == null) {
      throw new IllegalArgumentException("Unproper usage of SwtImageBuilder");
    }
    //Draw snowflakes
    gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
    gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
    List<Snowflake> snowflakes = generator.getMoveableObjects();
    FlagsConfiguration config = controller.getFlagsConfiguration();
    for (Snowflake snowflake : snowflakes) {
      if (config.isMercedesSnowflakes()) {
        GuiUtils.drawSnowflakeAsMercedes(gc, snowflake);
      } else if (snowflake.isShowHead()) {
        GuiUtils.draw(gc, snowflake);
      }
      if (!snowflake.isFreezed() && (config.isDebug() || config.isObjectsTail() || snowflake.isShowTrail())) {
        int counterAlpha = 0;
        int origAlpha = snowflake.getAlpha();
        double factorConsiderOrigAlpha = origAlpha / 255.0;
        int points = snowflake.getSnowTail().getHistoryLocations().size();
        for (Point2D oldLoc : snowflake.getSnowTail().getHistoryLocations()) {
          snowflake.setAlpha((int) (factorConsiderOrigAlpha * Utils.linearInterpolation(counterAlpha, 0, 0, points, 80)));
          GuiUtils.draw(gc, snowflake, oldLoc);
          counterAlpha++;
        }
        snowflake.setAlpha(origAlpha);
      }
    }
  }
}
