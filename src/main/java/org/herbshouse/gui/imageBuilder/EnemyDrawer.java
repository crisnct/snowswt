package org.herbshouse.gui.imageBuilder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.herbshouse.SnowingApplication;
import org.herbshouse.gui.GuiUtils;
import org.herbshouse.gui.SWTResourceManager;
import org.herbshouse.logic.AbstractMovableObject;
import org.herbshouse.logic.Generator;
import org.herbshouse.logic.enemies.AnimatedGif;
import org.herbshouse.logic.enemies.RedFace;

class EnemyDrawer {

  public void draw(GC gc, Generator<AbstractMovableObject> generator) {
    if (gc == null) {
      throw new IllegalArgumentException("Unproper usage of SwtImageBuilder");
    }
    gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
    gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
    for (AbstractMovableObject obj : generator.getMoveableObjects()) {
      if (obj instanceof RedFace redFace) {
        GuiUtils.drawRedFace(gc, redFace);
      } else if (obj instanceof AnimatedGif animatedGif) {
        Image img = SWTResourceManager.getGif(SnowingApplication.class,
            animatedGif.getFilename(),
            animatedGif.getImageIndex(),
            obj.getSize(),
            obj.getSize(),
            animatedGif.getRemoveBackgroundColor(),
            true
        );

        Point screenLoc = GuiUtils.toScreenCoord(animatedGif.getLocation());
        screenLoc.x -= obj.getSize() / 2;
        screenLoc.y -= obj.getSize() / 2;
        gc.drawImage(img, screenLoc.x, screenLoc.y);
        animatedGif.increaseImageIndex();
      }
    }
  }

}
