package org.herbshouse.controller;

import java.util.List;
import org.herbshouse.logic.AbstractMovableObject;
import org.herbshouse.logic.Generator;
import org.herbshouse.logic.UserInfo;

public interface MainController {

  boolean canStart();

  void flipImage();

  void pause();

  UserInfo getUserInfo();

  FlagsConfiguration getFlagsConfiguration();

  void registerGenerator(Generator<?> listener);

  void registerSoundController(SoundsController soundController);

  void registerViewController(ViewController viewController);

  List<Generator<? extends AbstractMovableObject>> getGenerators();

  int getDesiredFps();

  void reset();

  void shutdown();

  boolean isDemoRunning();

  void turnOffDemoMode();
}
