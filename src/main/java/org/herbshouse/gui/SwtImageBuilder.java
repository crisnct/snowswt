package org.herbshouse.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;
import org.herbshouse.logic.SnowGenerator;
import org.herbshouse.logic.Snowflake;
import org.herbshouse.logic.Point2D;

/**
 * This class is responsible for creating and managing an SWT Image that displays a snowy scene with a greeting text.
 * It utilizes the SWT graphics context (GC) to draw on an image, including a background, text, and snowflakes generated by a SnowGenerator.
 * The class supports flipping the image vertically and includes a debug mode to show the history of snowflake locations.
 * It implements AutoCloseable to ensure proper resource management, disposing of the graphics context, image, and transformation when done.
 *
 * @author cristian.tone
 */
public class SwtImageBuilder implements AutoCloseable {
    private final GC originalGC;
    private GC gcImage;
    private Transform transform;
    private Image image;

    public SwtImageBuilder(GC gc) {
        originalGC = gc;
    }

    public Image createImage(SnowGenerator snowGenerator, boolean flipImage, boolean debug) {
        Rectangle totalArea = originalGC.getClipping();
        image = new Image(Display.getDefault(), totalArea);
        gcImage = new GC(image);
        gcImage.setAdvanced(true);
        gcImage.setAntialias(SWT.DEFAULT);
        gcImage.setTextAntialias(SWT.ON);

        if (flipImage) {
            transform = new Transform(Display.getDefault());
            transform.scale(1, -1);
            transform.translate(0, -totalArea.height);
            gcImage.setTransform(transform);
        }

        //Draw background
        gcImage.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
        gcImage.fillRectangle(0, 0, totalArea.width, totalArea.height);

        //Draw text in middle of screen
        gcImage.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_CYAN));
        gcImage.setFont(SWTResourceManager.getFont("Arial", 25, SWT.BOLD));
        GuiUtils.drawTextInMiddleOfScreen(gcImage, "Happy New Year!");

        //Draw legend
        gcImage.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
        gcImage.setFont(SWTResourceManager.getFont("Arial", 15, SWT.BOLD));
        gcImage.drawText("Normal wind(space): ON\nHappy wind(X): OFF", totalArea.width - 300, 10, true);

        //Draw snowflakes
        gcImage.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        for (Snowflake snowflake : snowGenerator.getSnowflakes()) {
            GuiUtils.drawSnowflake(gcImage, snowflake);
            if (debug) {
                for (Point2D loc : snowflake.getHistoryLocations()) {
                    GuiUtils.drawSnowflake(gcImage, snowflake, loc);
                }
            }
        }
        return image;
    }

    @Override
    public void close() {
        if (transform != null) {
            transform.dispose();
        }
        gcImage.dispose();
        image.dispose();
    }

}
