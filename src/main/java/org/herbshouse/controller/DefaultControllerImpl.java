package org.herbshouse.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;
import org.herbshouse.audio.AudioPlayOrder;
import org.herbshouse.audio.AudioPlayType;
import org.herbshouse.audio.AudioPlayer;
import org.herbshouse.gui.GuiUtils;
import org.herbshouse.logic.AbstractMovableObject;
import org.herbshouse.logic.Generator;
import org.herbshouse.logic.Point2D;
import org.herbshouse.logic.UserInfo;
import org.herbshouse.logic.fractals.TreeType;

public class DefaultControllerImpl
    implements
    MainController,
    MouseController,
    SnowflakesController,
    FractalsController,
    SoundsController,
    RedfacesController,
    BlackholeController,
    ViewController,
    SelfController {

  private final FlagsConfiguration flagsConfiguration = new FlagsConfiguration();
  private final List<Generator<? extends AbstractMovableObject>> generators = new ArrayList<>();
  private final List<SoundsController> soundControllers = new ArrayList<>();
  private final List<ViewController> viewControllers = new ArrayList<>();

  private int desiredFPS;
  private UserInfo userInfo;
  private Transform transform;
  private int currentAttackPhase;
  private AudioPlayer audioPlayer;
  private final Timer timer;

  public DefaultControllerImpl() {
    timer = new Timer();
  }

  @Override
  public void registerGenerator(Generator<?> listener) {
    listener.setLogicController(this);
    listener.init(flagsConfiguration, GuiUtils.SCREEN_BOUNDS);
    generators.add(listener);
  }

  @Override
  public void registerSoundController(SoundsController soundController) {
    if (!soundControllers.contains(soundController)) {
      soundControllers.add(soundController);
    }
  }

  @Override
  public void registerViewController(ViewController viewController) {
    if (!viewControllers.contains(viewController)) {
      viewControllers.add(viewController);
    }
  }

  @Override
  public List<Generator<? extends AbstractMovableObject>> getGenerators() {
    return generators;
  }

  @Override
  public void mouseScrolled(int count) {
    getGenerators().forEach(l -> l.mouseScrolled(count));
  }

  @Override
  public void flipImage() {
    flagsConfiguration.switchFlipImage();
  }

  public FlagsConfiguration getFlagsConfiguration() {
    return flagsConfiguration;
  }

  @Override
  public UserInfo getUserInfo() {
    return userInfo;
  }

  public void setUserInfo(UserInfo userInfo) {
    this.userInfo = userInfo;
  }

  @Override
  public void switchNormalWind() {
    flagsConfiguration.switchNormalWind();
    if (flagsConfiguration.isNormalWind() && flagsConfiguration.isHappyWind()) {
      switchHappyWind();
    }
    this.checkTriggerWind();
  }

  private void checkTriggerWind() {
    if (flagsConfiguration.isNormalWind() && !flagsConfiguration.isHappyWind()) {
      AudioPlayOrder order = new AudioPlayOrder("sounds/wind.wav");
      order.setType(AudioPlayType.BACKGROUND);
      order.setVolume(0.7f);
      this.audioPlayer.play(order);
    } else {
      this.audioPlayer.stop("sounds/wind.wav");
    }
  }

  @Override
  public void switchHappyWind() {
    if (!flagsConfiguration.isHappyWind()) {
      generators.forEach(Generator::turnOnHappyWind);
    }
    flagsConfiguration.switchHappyWind();
    if (flagsConfiguration.isHappyWind() && flagsConfiguration.isNormalWind()) {
      switchNormalWind();
    }
    this.checkTriggerWind();
  }

  @Override
  public void pause() {
    flagsConfiguration.switchPause();
  }

  @Override
  public void reset() {
    generators.forEach(Generator::reset);
  }

  @Override
  public void switchBigBalls() {
    flagsConfiguration.switchBigBalls();
  }

  @Override
  public void switchDebug() {
    flagsConfiguration.switchDebug();
    generators.forEach(Generator::switchDebug);
  }

  @Override
  public void switchObjectsTail() {
    flagsConfiguration.switchObjectsTail();
  }

  @Override
  public void switchAttack() {
    flagsConfiguration.switchAttack();
    generators.forEach(Generator::switchAttack);
  }

  @Override
  public void setAttackType(int type) {
    final int oldType = flagsConfiguration.getAttackType();
    flagsConfiguration.setAttackType(type);
    if (flagsConfiguration.isAttack()) {
      generators.forEach(l -> l.changeAttackType(oldType, type));
    }
  }

  @Override
  public void switchMercedesSnowflakes() {
    flagsConfiguration.switchMercedesSnowflakes();
  }

  @Override
  public void increaseSnowLevel() {
    if (flagsConfiguration.getSnowingLevel() < 10) {
      flagsConfiguration.increaseSnowingLevel();
      generators.forEach(Generator::changedSnowingLevel);
    }
  }

  @Override
  public void decreaseSnowLevel() {
    if (flagsConfiguration.getSnowingLevel() > 0) {
      flagsConfiguration.decreaseSnowingLevel();
      generators.forEach(Generator::changedSnowingLevel);
    }
  }

  @Override
  public void switchYoutube() {
    flagsConfiguration.switchYoutube();
    Display.getDefault().syncExec(() -> {
      soundControllers.forEach(SoundsController::switchYoutube);
    });
  }

  @Override
  public void playNextYoutube() {
    Display.getDefault().syncExec(() -> {
      soundControllers.forEach(SoundsController::playNextYoutube);
    });
  }

  @Override
  public void switchEnemies() {
    flagsConfiguration.switchEnemies();
    if (flagsConfiguration.isEnemies()) {
      AudioPlayOrder order = new AudioPlayOrder("sounds/drum.wav");
      order.setType(AudioPlayType.BACKGROUND);
      this.audioPlayer.play(order);
    } else {
      this.audioPlayer.stop("sounds/drum.wav");
    }
  }

  @Override
  public void shutdown() {
    AudioPlayOrder order = new AudioPlayOrder("sounds/glass-breaking.wav");
    order.setCallback(() -> audioPlayer.shutdown());
    this.audioPlayer.play(order);
    generators.forEach(Generator::shutdown);
    timer.cancel();
    timer.purge();
  }

  @Override
  public void setAudio(AudioPlayer audioPlayer) {
    this.audioPlayer = audioPlayer;
  }

  @Override
  public void switchFractals() {
    flagsConfiguration.switchFractals();
  }

  @Override
  public void setFractalsType(TreeType treeType) {
    flagsConfiguration.setFractalsType(treeType);
    generators.forEach(Generator::changedFractalType);
  }

  @Override
  public void switchBlackHoles() {
    flagsConfiguration.switchBlackHoles();
    generators.forEach(Generator::switchBlackHoles);
  }

  @Override
  public void switchIndividualMovements() {
    flagsConfiguration.switchIndividualMovements();
    generators.forEach(Generator::switchIndividualMovements);
  }

  @Override
  public boolean canStart() {
    return generators.stream().allMatch(Generator::canControllerStart);
  }

  @Override
  public void switchGraphicalSounds() {
    flagsConfiguration.switchGraphicalSounds();
    generators.forEach(Generator::switchGraphicalSounds);
  }

  @Override
  public void setGraphicalSound(GraphicalSoundConfig graphicalSoundConfig) {
    flagsConfiguration.setGraphicalSoundConfig(graphicalSoundConfig);
    generators.forEach(l -> l.changeGraphicalSound(graphicalSoundConfig));
  }

  public AudioPlayer getAudioPlayer() {
    return audioPlayer;
  }

  public Transform getTransform() {
    return transform;
  }

  public void setTransform(Transform transform) {
    this.transform = transform;
  }

  @Override
  public int getCurrentAttackPhase() {
    return currentAttackPhase;
  }

  public void setCurrentAttackPhase(int currentAttackPhase) {
    this.currentAttackPhase = currentAttackPhase;
  }

  public void setDesiredFPS(int desiredFPS) {
    this.desiredFPS = desiredFPS;
  }

  @Override
  public int getDesiredFps() {
    return desiredFPS;
  }

  @Override
  public void mouseMove(int x, int y) {
    Point2D mouseLoc = GuiUtils.toWorldCoord(convertLoc(x, y));
    getFlagsConfiguration().setMouseCurrentLocation(mouseLoc);
    getGenerators().forEach(l -> l.mouseMove(mouseLoc));
  }

  @Override
  public void mouseDown(int button, int x, int y) {
    Point2D mouseLoc = GuiUtils.toWorldCoord(convertLoc(x, y));
    getGenerators().forEach(l -> l.mouseDown(button, mouseLoc));
  }

  private Point convertLoc(int x, int y) {
    int locX = x;
    int locY = y;
    if (getFlagsConfiguration().isFlipImage()) {
      float[] data = {locX, locY};
      getTransform().transform(data);
      locX = (int) data[0];
      locY = (int) data[1];
    }
    return new Point(locX, locY);
  }

  @Override
  public void startSelfControlling(boolean skipAnimation) {
    long durationLetItSnow = 60 + 52;
    long durationFractalsDraw = 37;
    long durationFrozenMusic = 3 * 60 + 41;
    long time = skipAnimation ? 1 : 40;

    this.scheduleTask(time, this::switchFractals);

    //Play Let it snow
    this.scheduleTask(time, this::switchYoutube);
    this.scheduleTask(time + 30, () -> {
      if (!flagsConfiguration.isAttack()) {
        switchAttack();
      }
    });
    this.scheduleTask(time + 60, () -> {
      setAttackType(2);
    });
    this.scheduleTask(time + 90, () -> {
      setAttackType(3);
    });
    this.scheduleTask(time + 120, () -> {
      setAttackType(4);
    });
    this.scheduleTask(time + durationLetItSnow, this::switchYoutube);

    //Fractals
    time += durationFractalsDraw;
    this.scheduleTask(time, () -> setFractalsType(TreeType.RANDOM_DEFAULT));
    time += durationFractalsDraw;
    this.scheduleTask(time, () -> setFractalsType(TreeType.PERFECT_FIR));
    time += durationFractalsDraw;
    this.scheduleTask(time, () -> setFractalsType(TreeType.RANDOM_FIR));
    time += durationFractalsDraw;

    // Start play Frozen
    this.scheduleTask(time, () -> {
      switchYoutube();
      if (flagsConfiguration.isAttack()) {
        switchAttack();
      }
      if (flagsConfiguration.isFractals()) {
        switchFractals();
      }
    });

    //Stop frozen music and wind
    this.scheduleTask(time + durationFrozenMusic, () -> {
      switchYoutube();
      switchNormalWind();
    });
    time += 35;
    //Start wind
    this.scheduleTask(time, this::switchNormalWind);

  }

  private void scheduleTask(long delaySeconds, Runnable task) {
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        task.run();
      }
    }, delaySeconds * 1000);
  }

  @Override
  public void substractAreaFromShell(int[] polygon) {
    Display.getDefault().asyncExec(() -> {
      viewControllers.forEach(view -> view.substractAreaFromShell(polygon));
    });
  }

  @Override
  public void resetScreenSurface() {
    Display.getDefault().asyncExec(() -> {
      viewControllers.forEach(ViewController::resetScreenSurface);
    });
  }

}
