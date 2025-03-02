package org.herbshouse.controller;

import org.herbshouse.logic.Point2D;
import org.herbshouse.logic.fractals.TreeType;

public class FlagsConfiguration {

  private boolean normalWind;
  private boolean happyWind = true;
  private boolean debug;
  private boolean attack;
  private boolean flipImage;
  private boolean bigBalls;
  private boolean pause;
  private boolean mercedesSnowflakes;
  private boolean enemies = false;
  private boolean youtube;
  private boolean objectsTail;
  private boolean fractals;
  private boolean blackHoles;
  private boolean individualMovements;
  private boolean graphicalSounds;

  private int attackType = 1;
  private int snowingLevel = 1;
  private Point2D mouseLoc = new Point2D();
  private TreeType fractalsType = TreeType.PERFECT_DEFAULT;
  private GraphicalSoundConfig graphicalSoundConfig
      = new GraphicalSoundConfig(75, 77, 30, 10, true, 5, 2, 4);

  void increaseSnowingLevel() {
    this.snowingLevel++;
  }

  void decreaseSnowingLevel() {
    this.snowingLevel--;
  }

  void switchMercedesSnowflakes() {
    this.mercedesSnowflakes = !mercedesSnowflakes;
  }

  void switchPause() {
    this.pause = !pause;
  }

  void switchYoutube() {
    this.youtube = !youtube;
  }

  void switchBigBalls() {
    this.bigBalls = !bigBalls;
  }

  void switchAttack() {
    this.attack = !attack;
  }

  void switchEnemies() {
    this.enemies = !enemies;
  }

  protected void switchObjectsTail() {
    objectsTail = !objectsTail;
  }

  public boolean isObjectsTail() {
    return objectsTail;
  }

  void switchDebug() {
    this.debug = !debug;
  }

  void switchFlipImage() {
    this.flipImage = !flipImage;
  }

  void switchNormalWind() {
    this.normalWind = !normalWind;
  }

  void switchHappyWind() {
    this.happyWind = !happyWind;
  }

  public boolean isYoutube() {
    return youtube;
  }

  public int getAttackType() {
    return attackType;
  }

  void setAttackType(int i) {
    this.attackType = i;
  }

  public boolean isDebug() {
    return debug;
  }

  public boolean isMercedesSnowflakes() {
    return mercedesSnowflakes;
  }

  public boolean isFlipImage() {
    return flipImage;
  }

  public boolean isHappyWind() {
    return happyWind;
  }

  public boolean isNormalWind() {
    return normalWind;
  }

  public boolean isBigBalls() {
    return bigBalls;
  }

  public boolean isPause() {
    return pause;
  }

  public int getSnowingLevel() {
    return snowingLevel;
  }

  public void setMouseCurrentLocation(Point2D mouseLoc) {
    this.mouseLoc = mouseLoc;
  }

  public Point2D getMouseLoc() {
    return mouseLoc;
  }

  public boolean isEnemies() {
    return enemies;
  }

  public boolean isAttack() {
    return attack;
  }

  void switchFractals() {
    this.fractals = !fractals;
  }

  public boolean isFractals() {
    return fractals;
  }

  public TreeType getFractalsType() {
    return fractalsType;
  }

  void setFractalsType(TreeType treeType) {
    this.fractalsType = treeType;
  }

  void switchBlackHoles() {
    this.blackHoles = !blackHoles;
  }

  public boolean isBlackHoles() {
    return blackHoles;
  }

  void switchIndividualMovements() {
    this.individualMovements = !individualMovements;
  }

  public boolean isIndividualMovements() {
    return individualMovements;
  }

  void switchGraphicalSounds() {
    this.graphicalSounds = !graphicalSounds;
  }

  public boolean isGraphicalSounds() {
    return graphicalSounds;
  }

  public GraphicalSoundConfig getGraphicalSoundConfig() {
    return graphicalSoundConfig;
  }

  public void setGraphicalSoundConfig(GraphicalSoundConfig graphicalSoundConfig) {
    this.graphicalSoundConfig = graphicalSoundConfig;
  }
}
