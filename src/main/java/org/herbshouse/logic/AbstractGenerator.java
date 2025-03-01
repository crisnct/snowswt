package org.herbshouse.logic;

import org.eclipse.swt.graphics.Rectangle;
import org.herbshouse.audio.AudioPlayOrder;
import org.herbshouse.controller.DefaultControllerImpl;
import org.herbshouse.controller.FlagsConfiguration;
import org.herbshouse.controller.GraphicalSoundConfig;
import org.herbshouse.controller.ViewController;

public abstract class AbstractGenerator<T extends AbstractMovableObject>
    extends Thread implements Generator<T>, ViewController {

  protected FlagsConfiguration config;

  protected Rectangle screenBounds;

  private ViewController viewController;

  private DefaultControllerImpl logicController;

  public DefaultControllerImpl getLogicController() {
    return logicController;
  }

  @Override
  public abstract void run();

  @Override
  public void setLogicController(DefaultControllerImpl logicController) {
    this.logicController = logicController;
  }

  protected abstract int getSleepDuration();

  protected int getSleepDurationDoingNothing() {
    return 100;
  }

  public void setViewController(ViewController viewController) {
    this.viewController = viewController;
  }

  @Override
  public void init(FlagsConfiguration flagsConfiguration, Rectangle screenBounds) {
    this.config = flagsConfiguration;
    this.screenBounds = screenBounds;
  }

  @Override
  public void substractAreaFromShell(int[] polygon) {
    viewController.substractAreaFromShell(polygon);
    AudioPlayOrder order = new AudioPlayOrder("sounds/hole-in-the-shell.wav", 1500);
    order.setVolume(0.9f);
    getLogicController().getAudioPlayer().play(order);
  }

  @Override
  public void resetScreenSurface() {
    viewController.resetScreenSurface();
  }

  public Rectangle getScreenBounds() {
    return screenBounds;
  }

  public FlagsConfiguration getFlagsConfiguration() {
    return config;
  }

  @Override
  public void changeGraphicalSound(GraphicalSoundConfig graphicalSoundConfig) {

  }
}
