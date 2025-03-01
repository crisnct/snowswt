package org.herbshouse.logic.enemies;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.herbshouse.audio.AudioPlayOrder;
import org.herbshouse.controller.FlagsConfiguration;
import org.herbshouse.controller.ViewController;
import org.herbshouse.gui.GuiUtils;
import org.herbshouse.logic.AbstractGenerator;
import org.herbshouse.logic.AbstractMovableObject;
import org.herbshouse.logic.Point2D;
import org.herbshouse.logic.Utils;

public class EnemyGenerator extends AbstractGenerator<AbstractMovableObject> {

  public static final RGB RED_COLOR = new RGB(160, 0, 0);
  public static final RGB FREE_MOVE_COLOR = new RGB(50, 180, 180);
  private static final RGB REMOVE_BACKGROUND_COLOR = new RGB(255, 255, 255);
  private static final int ANGRY_FACE_SIZE = 150;

  private final List<RedFace> redFaces = new CopyOnWriteArrayList<>();
  private final List<AnimatedGif> fireGifs = new CopyOnWriteArrayList<>();
  private AnimatedGif angryFace;
  private boolean shutdown = false;

  @Override
  public void run() {
    Timer timer = new Timer("EnemiesTimer");
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        if (config.isEnemies() && !shutdown && !getFlagsConfiguration().isPause()) {
          generateRedFace();
        }
      }
    }, 5000, 10000);
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        if (config.isEnemies() && !shutdown && !getFlagsConfiguration().isPause()) {
          for (RedFace redFace : redFaces) {
            if (config.isAttack()) {
              redFace.setState(RedFaceState.FOLLOW_MOUSE);
            }
            if (redFace.getKissingGif() == null) {
              redFace.decreaseLife(1);
            } else {
              switch (redFace.getState()) {
                case FOLLOW_MOUSE -> getLogicController().getUserInfo().decreasePoints(1);
                case FREE -> getLogicController().getUserInfo().increasePoints(1);
              }
            }
          }
        }
      }
    }, 1000, 1000);

    while (!shutdown) {
      if (config.isEnemies()) {
        if (!getFlagsConfiguration().isPause()) {
          this.move();
          this.checkCollisions();
          this.cleanupFire();
          Utils.sleep(getSleepDuration());
        } else {
          Utils.sleep(getSleepDurationDoingNothing());
        }
      } else {
        this.cleanup();
        Utils.sleep(getSleepDurationDoingNothing());
      }
    }

    this.cleanup();
    timer.cancel();
    timer.purge();
  }

  private void cleanupFire() {
    List<AnimatedGif> toRemove = new ArrayList<>();
    for (AnimatedGif fire : fireGifs) {
      if (fire.getLocation().y > screenBounds.height) {
        toRemove.add(fire);
      }
    }
    if (!toRemove.isEmpty()) {
      fireGifs.removeAll(toRemove);
    }
  }

  private void removeRedFace(RedFace redFace) {
    redFace.stopTimer();
    redFaces.remove(redFace);
  }

  private void move() {
    boolean kiss = false;
    for (RedFace redFace : redFaces) {
      if (redFace.getLife() == 0) {
        double[] circlePoints
            = Utils.generateCircle(redFace.getLocation(), redFace.getSize() / 2.0d,
            redFace.getSize(), 0);
        if (getLogicController() instanceof ViewController viewController) {
          viewController.substractAreaFromShell(GuiUtils.toScreenCoord(circlePoints));
          AudioPlayOrder order = new AudioPlayOrder("sounds/hole-in-the-shell.wav", 1500);
          order.setVolume(0.9f);
          getLogicController().getAudioPlayer().play(order);
        }
        this.removeRedFace(redFace);
        continue;
      }
      boolean isMouseInTheBall
          = Utils.distance(redFace.getLocation(), config.getMouseLoc())
          < redFace.getSize() / 2.0d;
      if (isMouseInTheBall && redFace.getKissingGif() == null) {
        redFace.startKissing();
      } else if (!isMouseInTheBall && redFace.getKissingGif() != null) {
        redFace.stopKissing();
        if (redFace.getState() == RedFaceState.FOLLOW_MOUSE) {
          redFace.setDirection(
              Utils.angleOfLine(redFace.getLocation(), config.getMouseLoc()));
        }
      } else {
        redFace.move();
      }

      kiss = kiss || redFace.getKissingGif() != null;
    }

    if (kiss && !getLogicController().getAudioPlayer().isPlaying("sounds/kissing.wav")) {
      AudioPlayOrder order = new AudioPlayOrder("sounds/kissing.wav");
      order.setVolume(0.9f);
      getLogicController().getAudioPlayer().play(order);
    }

    for (AnimatedGif fire : fireGifs) {
      fire.move();
      if (fire.getLocation().y < 0) {
        fireGifs.remove(fire);
      }
    }
  }

  @Override
  public void turnOnHappyWind() {

  }

  @Override
  public void switchDebug() {

  }

  @Override
  public void mouseMove(Point2D mouseLocation) {
    for (RedFace redFace : redFaces) {
      boolean isMouseInTheBall = Utils.distance(redFace.getLocation(), config.getMouseLoc()) < redFace.getSize() / 2.0d;
      if (!isMouseInTheBall && redFace.getKissingGif() != null) {
        redFace.stopKissing();
      }
    }
  }

  @Override
  public void mouseDown(int button, Point2D mouseLocation) {
    if (button == 1 && config.isEnemies()) {
      AnimatedGif gif = new AnimatedGif("pictures/fire-flame.gif", 2, null);
      gif.setLocation(mouseLocation);
      gif.setSize(50);
      gif.setSpeed(2);
      fireGifs.add(gif);
      getLogicController().getAudioPlayer().play(new AudioPlayOrder("sounds/fire.wav", 700));
    }
  }

  @Override
  public void mouseScrolled(int count) {

  }

  @Override
  public void reset() {
    cleanup();
    this.generateRedFace();
    getLogicController().resetScreenSurface();
  }

  private void cleanup() {
    for (RedFace redFace : redFaces) {
      redFace.stopTimer();
    }
    this.redFaces.clear();
    this.fireGifs.clear();
  }

  @Override
  public void init(FlagsConfiguration flagsConfiguration, Rectangle screenBounds) {
    super.init(flagsConfiguration, screenBounds);

    this.generateRedFace();

    this.angryFace = new AnimatedGif("pictures/angry.gif", 0.3, REMOVE_BACKGROUND_COLOR);
    this.angryFace.setLocation(
        new Point2D(screenBounds.width - ANGRY_FACE_SIZE / 2.0d, ANGRY_FACE_SIZE / 2.0d));
    this.angryFace.setSize(ANGRY_FACE_SIZE);
    this.angryFace.setSpeed(1);
  }

  private void generateRedFace() {
    RedFace redFace = new RedFace();
    int size = 50 + (int) (Math.random() * 100);
    redFace.setLocation(new Point2D(screenBounds.width - size / 2.0d - 10, size / 2.0d + 10));
    redFace.setSize(size);
    redFace.setLife((int) Utils.linearInterpolation(size, 50, 30, 150, 120));
    redFace.setSpeed(1);
    redFace.setDirection(Math.toRadians(89 + Math.random() * 91));
    redFace.setState(RedFaceState.FREE);
    redFace.setStateLazy(10000, RedFaceState.FOLLOW_MOUSE);
    redFaces.add(redFace);
  }

  @Override
  public List<AbstractMovableObject> getMoveableObjects() {
    List<AbstractMovableObject> enemies = new ArrayList<>();
    for (RedFace redFace : redFaces) {
      enemies.add(redFace);
      if (redFace.getKissingGif() != null) {
        enemies.add(redFace.getKissingGif());
      }
    }
    if (angryFace != null && config.isEnemies()) {
      enemies.add(angryFace);
    }
    enemies.addAll(fireGifs);
    return enemies;
  }

  @Override
  public void shutdown() {
    shutdown = true;
  }

  @Override
  public void provideImageData(ImageData imageData) {
  }

  @Override
  public void switchAttack() {

  }

  @Override
  public void changeAttackType(int oldType, int newType) {

  }

  private void checkCollisions() {
    for (int i = 0; i < redFaces.size(); i++) {
      //Check collision with walls
      RedFace redFace1 = redFaces.get(i);
      this.checkWallCollision(redFace1);
      this.checkFireCollision(redFace1);
      for (int j = i + 1; j < redFaces.size(); j++) {
        RedFace redFace2 = redFaces.get(j);
        if (Utils.isColliding(redFace1, redFace2)) {
          this.computeNewDirections(redFace1, redFace2);
        }
      }
    }
  }

  private void computeNewDirections(RedFace redFace1, RedFace redFace2) {
    double m1 = redFace1.getSize(); // mass of ball 1
    double m2 = redFace2.getSize(); // mass of ball 2

    Point2D vel1 = Utils.moveToDirection(new Point2D(0, 0), redFace1.getSpeed(),
        redFace1.getDirection());
    Point2D vel2 = Utils.moveToDirection(new Point2D(0, 0), redFace2.getSpeed(),
        redFace2.getDirection());

    // Calculate the velocity of the center of mass
    double VcmX = (m1 * vel1.x + m2 * vel2.x) / (m1 + m2);
    double VcmY = (m1 * vel1.y + m2 * vel2.y) / (m1 + m2);

    // Calculate the final velocities in the lab frame
    double finalVelocity1X = -vel1.x + VcmX;
    double finalVelocity1Y = -vel1.y + VcmY;
    double finalVelocity2X = -vel2.x + VcmX;
    double finalVelocity2Y = -vel2.y + VcmY;

    // Calculate the angles (directions) of the final velocities
    double angle1 = Math.atan2(finalVelocity1Y, finalVelocity1X);
    double angle2 = Math.atan2(finalVelocity2Y, finalVelocity2X);

    Point2D newLoc1 = Utils.moveToDirection(redFace1.getLocation(), 5, angle1);
    Point2D newLoc2 = Utils.moveToDirection(redFace2.getLocation(), 5, angle2);

    double distance = Utils.distance(newLoc1, newLoc2);
    double collisionDistance = (redFace1.getSize() + redFace2.getSize()) / 2.0 + 2;

    // Early return if the distance is too small
    if (distance > collisionDistance) {
      updateRedFace(redFace1, angle1);
      updateRedFace(redFace2, angle2);
    }
  }

  private void updateRedFace(RedFace redFace, double angle) {
    if (!redFace.isPause()) {
      redFace.setDirection(angle);
      if (redFace.getState() == RedFaceState.FOLLOW_MOUSE) {
        redFace.setState(RedFaceState.FREE);
        redFace.setStateLazy(10000, RedFaceState.DAMAGED);
        redFace.setStateLazy(11000, RedFaceState.FOLLOW_MOUSE);
      }
    }
  }

  private void checkFireCollision(RedFace redFace) {
    for (AnimatedGif fire : fireGifs) {
      if (Utils.isColliding(redFace, fire)) {
        getLogicController().getAudioPlayer().play(new AudioPlayOrder("sounds/hurt.wav"));
        if (redFace.getState() == RedFaceState.FREE) {
          redFace.decreaseLife(20);
          if (redFace.getLife() == 0) {
            this.removeRedFace(redFace);
          } else {
            redFace.setState(RedFaceState.DAMAGED);
            redFace.setStateLazy(3000, RedFaceState.FREE);
          }
        } else if (redFace.getState() == RedFaceState.FOLLOW_MOUSE) {
          redFace.setState(RedFaceState.DAMAGED);
          redFace.setStateLazy(1000, RedFaceState.FREE);
          redFace.setStateLazy(5000, RedFaceState.FOLLOW_MOUSE);
        }
        fireGifs.remove(fire);
        break;
      }
    }
  }

  private void checkWallCollision(RedFace redFace) {
    Point2D vel = Utils.moveToDirection(new Point2D(0, 0), redFace.getSpeed(),
        redFace.getDirection());
    int radius = redFace.getSize() / 2;
    double x = redFace.getLocation().x;
    double y = redFace.getLocation().y;

    boolean colision = false;
    // Check for collision with the left or right bounds
    if (x - radius < 0) {
      x = radius; // Correct position
      vel.x = -vel.x; // Reverse the horizontal direction
      colision = true;
    } else if (x + radius > screenBounds.width) {
      x = screenBounds.width - radius; // Correct position
      vel.x = -vel.x; // Reverse the horizontal direction
      colision = true;
    }

    // Check for collision with the top or bottom bounds
    if (y - radius < 0) {
      y = radius;// Correct position
      vel.y = -vel.y; // Reverse the vertical direction
      colision = true;
    } else if (y + radius > screenBounds.height) {
      y = screenBounds.height - radius; // Correct position
      vel.y = -vel.y; // Reverse the vertical direction
      colision = true;
    }

    if (colision) {
      redFace.setLocation(new Point2D(x, y));
      redFace.setDirection(Math.atan2(vel.y, vel.x));
    }
  }

  @Override
  protected int getSleepDuration() {
    return 2;
  }

  @Override
  public void switchBlackHoles() {

  }

  @Override
  public void switchIndividualMovements() {

  }

  @Override
  public boolean canControllerStart() {
    return true;
  }

  @Override
  public void switchGraphicalSounds() {

  }

}
