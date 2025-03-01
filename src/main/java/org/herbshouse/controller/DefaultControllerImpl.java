package org.herbshouse.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.herbshouse.audio.AudioPlayOrder;
import org.herbshouse.audio.AudioPlayType;
import org.herbshouse.audio.AudioPlayer;
import org.herbshouse.gui.GuiUtils;
import org.herbshouse.logic.AbstractMovableObject;
import org.herbshouse.logic.GeneratorListener;
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
    SelfController {

  private final FlagsConfiguration flagsConfiguration = new FlagsConfiguration();
  private final List<GeneratorListener<? extends AbstractMovableObject>> listeners = new ArrayList<>();
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
  public void registerListener(GeneratorListener<?> listener) {
    listener.setLogicController(this);
    listener.init(flagsConfiguration, GuiUtils.SCREEN_BOUNDS);
    listeners.add(listener);
  }

  @Override
  public List<GeneratorListener<? extends AbstractMovableObject>> getListeners() {
    return listeners;
  }

  @Override
  public void mouseScrolled(int count) {
    getListeners().forEach(l -> l.mouseScrolled(count));
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
      listeners.forEach(GeneratorListener::turnOnHappyWind);
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
    listeners.forEach(GeneratorListener::reset);
  }

  @Override
  public void switchBigBalls() {
    flagsConfiguration.switchBigBalls();
  }

  @Override
  public void switchDebug() {
    flagsConfiguration.switchDebug();
    listeners.forEach(GeneratorListener::switchDebug);
  }

  @Override
  public void switchObjectsTail() {
    flagsConfiguration.switchObjectsTail();
  }

  @Override
  public void switchAttack() {
    flagsConfiguration.switchAttack();
    listeners.forEach(GeneratorListener::switchAttack);
  }

  @Override
  public void setAttackType(int type) {
    final int oldType = flagsConfiguration.getAttackType();
    flagsConfiguration.setAttackType(type);
    if (flagsConfiguration.isAttack()) {
      listeners.forEach(l -> l.changeAttackType(oldType, type));
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
      listeners.forEach(GeneratorListener::changedSnowingLevel);
    }
  }

  @Override
  public void decreaseSnowLevel() {
    if (flagsConfiguration.getSnowingLevel() > 0) {
      flagsConfiguration.decreaseSnowingLevel();
      listeners.forEach(GeneratorListener::changedSnowingLevel);
    }
  }

  @Override
  public void switchYoutube() {
    flagsConfiguration.switchYoutube();
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
    listeners.forEach(GeneratorListener::shutdown);
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
    listeners.forEach(GeneratorListener::changedFractalType);
  }

  @Override
  public void switchBlackHoles() {
    flagsConfiguration.switchBlackHoles();
    listeners.forEach(GeneratorListener::switchBlackHoles);
  }

  @Override
  public void switchIndividualMovements() {
    flagsConfiguration.switchIndividualMovements();
    listeners.forEach(GeneratorListener::switchIndividualMovements);
  }

  @Override
  public boolean canStart() {
    return listeners.stream().allMatch(GeneratorListener::canControllerStart);
  }

  @Override
  public void switchGraphicalSounds() {
    flagsConfiguration.switchGraphicalSounds();
    listeners.forEach(GeneratorListener::switchGraphicalSounds);
  }

  @Override
  public void setGraphicalSound(GraphicalSoundConfig graphicalSoundConfig) {
    flagsConfiguration.setGraphicalSoundConfig(graphicalSoundConfig);
    listeners.forEach(l -> l.changeGraphicalSound(graphicalSoundConfig));
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
    getListeners().forEach(l -> l.mouseMove(mouseLoc));
  }

  @Override
  public void mouseDown(int button, int x, int y) {
    Point2D mouseLoc = GuiUtils.toWorldCoord(convertLoc(x, y));
    getListeners().forEach(l -> l.mouseDown(button, mouseLoc));
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
    long start = 1;
    long durationLetItSnow = 112;
    long durationFractalsDraw = 37;
    long timeOffset = skipAnimation ? start : 30;
    timeOffset += durationLetItSnow;
    long durationFrozenMusic = 3 * 60 + 41;

    this.scheduleTask(start, () -> GuiUtils.postKeyEvent('F'));
    this.scheduleTask(start, () -> GuiUtils.postKeyEvent('Y'));

    start += durationFractalsDraw;
    this.scheduleTask(start, () -> GuiUtils.postKeyEvent(SWT.F2));
    start += durationFractalsDraw;
    this.scheduleTask(start, () -> GuiUtils.postKeyEvent(SWT.F3));
    start += durationFractalsDraw;
    this.scheduleTask(start, () -> GuiUtils.postKeyEvent(SWT.F4));
    //Turn off music let it snow
    this.scheduleTask(timeOffset, () -> GuiUtils.postKeyEvent('Y'));

    //Starts attack
    this.scheduleTask(timeOffset + 3, () -> {
      setAttackType(3);
      if (!flagsConfiguration.isAttack()) {
        switchAttack();
      }
    });

    start += durationFractalsDraw;
    // Start frozen music
    this.scheduleTask(start, () -> {
      GuiUtils.postKeyEvent('Y');
      if (flagsConfiguration.isAttack()) {
        switchAttack();
      }
      if (flagsConfiguration.isFractals()) {
        switchFractals();
      }
    });

    //Stop frozen music and wind
    this.scheduleTask(start + durationFrozenMusic, () -> {
      GuiUtils.postKeyEvent('Y');
      GuiUtils.postKeyEvent(' ');
    });
    start += 35;
    //Start wind
    this.scheduleTask(start, () -> {
      GuiUtils.postKeyEvent(' ');
    });

  }

  private void scheduleTask(long delaySeconds, Runnable task) {
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        task.run();
      }
    }, delaySeconds * 1000);
  }

}
