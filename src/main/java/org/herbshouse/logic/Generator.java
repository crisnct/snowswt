package org.herbshouse.logic;

import java.util.List;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.herbshouse.controller.DefaultControllerImpl;
import org.herbshouse.controller.FlagsConfiguration;
import org.herbshouse.controller.GraphicalSoundConfig;

public interface Generator<T extends AbstractMovableObject> {

  void turnOnHappyWind();

  void switchDebug();

  void mouseMove(Point2D mouseLocation);

  void mouseDown(int button, Point2D mouseLocation);

  void mouseScrolled(int count);

  void reset();

  void init(FlagsConfiguration flagsConfiguration, Rectangle screenBounds);

  default int getCountdown() {
    return -1;
  }

  List<T> getMoveableObjects();

  void shutdown();

  void provideImageData(ImageData imageData);

  void switchAttack();

  default void changedSnowingLevel() {
  }

  default void changedFractalType() {

  }

  void changeAttackType(int oldAttackType, int newAttackType);

  void setLogicController(DefaultControllerImpl logicController);

  void switchBlackHoles();

  void switchIndividualMovements();

  boolean canControllerStart();

  void changeGraphicalSound(GraphicalSoundConfig graphicalSoundConfig);

  void switchGraphicalSounds();
}
