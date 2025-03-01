package org.herbshouse.logic;

import org.eclipse.swt.graphics.Rectangle;
import org.herbshouse.controller.DefaultControllerImpl;
import org.herbshouse.controller.FlagsConfiguration;
import org.herbshouse.controller.GraphicalSoundConfig;

public abstract class AbstractGenerator<T extends AbstractMovableObject> extends Thread implements Generator<T> {

  protected FlagsConfiguration config;

  protected Rectangle screenBounds;

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

  @Override
  public void init(FlagsConfiguration flagsConfiguration, Rectangle screenBounds) {
    this.config = flagsConfiguration;
    this.screenBounds = screenBounds;
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
