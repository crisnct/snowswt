package org.herbshouse.logic.snow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.herbshouse.controller.FlagsConfiguration;
import org.herbshouse.gui.GuiUtils;
import org.herbshouse.logic.AbstractGenerator;
import org.herbshouse.logic.Point2D;
import org.herbshouse.logic.Utils;
import org.herbshouse.logic.snow.attack.AbstractPhaseAttackData;
import org.herbshouse.logic.snow.attack.AttackStrategy;
import org.herbshouse.logic.snow.attack.impl.nonphase.BigWormAttackStrategy;
import org.herbshouse.logic.snow.attack.impl.nonphase.FireworksStrategy;
import org.herbshouse.logic.snow.attack.impl.nonphase.YinYangAttackStrategy;
import org.herbshouse.logic.snow.attack.impl.phase.dancing.DancingSnowflakesStrategy;
import org.herbshouse.logic.snow.attack.impl.phase.parasites.ParasitesAttackStrategy;
import org.herbshouse.logic.snow.data.AbstractAttackData;
import org.herbshouse.logic.snow.snowing.HappyWindSnowing;
import org.herbshouse.logic.snow.snowing.NoWindSnowing;
import org.herbshouse.logic.snow.snowing.NormalWindSnowing;

public class SnowGenerator extends AbstractGenerator<Snowflake> {

  private final List<Snowflake> snowflakes = new CopyOnWriteArrayList<>();
  private final ReentrantLock lockSnowflakes = new ReentrantLock(false);
  private final Timer timer;
  private final Map<Integer, AttackStrategy<?>> attackStrategies = new HashMap<>();
  private final InitialAnimation initialAnimation;

  private NormalWindSnowing normalWindSnowing;
  private HappyWindSnowing happyWindSnowing;
  private NoWindSnowing noWindSnowing;
  private ImageData imageData;
  private TimerTask taskSnowing;
  private boolean shutdown = false;
  private boolean skipInitialAnimation;
  private boolean pauseSnowing;
  private int counterFreezeSnowklakes;
  private int snowflakesTimeGen = 5;
  private int slowCounter;

  public SnowGenerator() {
    timer = new Timer("SnowGeneratorTimer");
    initialAnimation = new InitialAnimation(this);
  }

  @Override
  public List<Snowflake> getMoveableObjects() {
    return snowflakes;
  }

  @Override
  public void run() {
    if (!skipInitialAnimation) {
      initialAnimation.run();
    }
    this.resetSnowingTimer();
    while (!shutdown) {
      if (!getFlagsConfiguration().isPause()) {
        this.update();
        if (!config.isAttack() && counterFreezeSnowklakes < 3000 && !config.isDebug()) {
          this.checkCollisions();
        }
        Utils.sleep(getSleepDuration());
      } else {
        Utils.sleep(getSleepDurationDoingNothing());
      }
    }

    removeSnowflakes(snowflakes);
    attackStrategies.values().forEach(AttackStrategy::shutdown);
    taskSnowing.cancel();
    timer.cancel();
    timer.purge();
  }

  void removeSnowflakes(List<Snowflake> snowflakeList) {
    for (Snowflake snowflake : snowflakeList) {
      if (snowflake.getIndividualStrategy() != null) {
        snowflake.getIndividualStrategy().shutdown();
      }
    }
    snowflakes.removeAll(snowflakeList);
  }

  void update() {
    if (lockSnowflakes.tryLock()) {
      //Move all snowflakes
      Snowflake prevSnowFlake = null;
      int snowflakeindex = 0;
      counterFreezeSnowklakes = 0;
      List<Snowflake> toRemove = new ArrayList<>();

      for (Snowflake snowflake : snowflakes) {
        if (snowflake.isFreezed()) {
          counterFreezeSnowklakes++;
          continue;
        }
        boolean attackMode = this.move(snowflake, prevSnowFlake, snowflakeindex);
        if (snowflake.getLocation().y <= 0 && !attackMode) {
          toRemove.add(snowflake);
        } else {
          prevSnowFlake = snowflake;
        }
        snowflakeindex++;
      }

      if (!toRemove.isEmpty()) {
        removeSnowflakes(toRemove);
      }

      if (!snowflakes.isEmpty()) {
        AttackStrategy<?> strategy = getAttackStrategy();
        if (config.isAttack()) {
          if (strategy.isStarted()) {
            strategy.afterUpdate(snowflakes);
            AbstractAttackData data = strategy.getData(snowflakes.getFirst());
            int phase = 0;
            if (data instanceof AbstractPhaseAttackData) {
              phase = ((AbstractPhaseAttackData) data).getPhase();
            }
            getLogicController().setCurrentAttackPhase(phase);
          }
        }
        for (Snowflake snowflake : snowflakes) {
          if (snowflake.getIndividualStrategy() != null && snowflake.getIndividualStrategy().isStarted()) {
            snowflake.getIndividualStrategy().afterUpdate(List.of(snowflake));
          }
        }
      }

      if (snowflakes.isEmpty() && config.isDebug()) {
        generateNewSnowflake(20, 1);
      }
      lockSnowflakes.unlock();
    }
  }

  private AttackStrategy<?> getAttackStrategy() {
    return attackStrategies.get(config.getAttackType());
  }

  @Override
  public int getCountdown() {
    return initialAnimation.getCountdown();
  }

  boolean move(Snowflake snowflake, Snowflake prevSnowFlake, int index) {
    snowflake.getSnowTail().registerHistoryLocation();
    AttackStrategy<?> attackStrategy = getAttackStrategy();
    boolean attackMode = config.isAttack() && attackStrategy.isStarted()
        && index < attackStrategy.getMaxSnowflakesInvolved();

    final Point2D nextLocation;
    if (snowflake.getIndividualStrategy() != null && snowflake.getIndividualStrategy().isStarted()) {
      nextLocation = snowflake.getIndividualStrategy().computeNextLocation(snowflake, prevSnowFlake);
    } else if (attackMode) {
      nextLocation = attackStrategy.computeNextLocation(snowflake, prevSnowFlake);
    } else if (config.isHappyWind()) {
      nextLocation = this.happyWindSnowing.computeNextLocation(snowflake);
    } else if (config.isNormalWind()) {
      nextLocation = this.normalWindSnowing.computeNextLocation(snowflake);
    } else {
      nextLocation = this.noWindSnowing.computeNextLocation(snowflake);
    }
    snowflake.setLocation(nextLocation);
    return attackMode;
  }

  @SuppressWarnings("UnusedReturnValue")
  public Snowflake generateNewSnowflake() {
    final int size;
    if (config.isBigBalls()) {
      size = 12 + (int) (Math.random() * 20);
    } else {
      size = 2 + (int) (Math.random() * 6);
    }
    final double speed;
    int snowingLevel = config.getSnowingLevel();
    if (snowingLevel < 3) {
      speed = 0.5 + Math.random();
    } else if (snowingLevel < 6) {
      speed = 0.5 + Math.random() * 2;
    } else {
      speed = 0.7 + Math.random() * 2.5;
    }
    return generateNewSnowflake(size, speed);
  }

  private Snowflake generateNewSnowflake(int size, double speed) {
    final Snowflake snowflake = new Snowflake();
    snowflake.setLocation(new Point2D(Math.random() * screenBounds.width, screenBounds.height));
    snowflake.setSize(size);
    snowflake.setSpeed(speed);
    if (config.isHappyWind()) {
      happyWindSnowing.initializeSnowFlakeHappyWind(snowflake);
    }
    snowflakes.add(snowflake);
    return snowflake;
  }

  @Override
  public void turnOnHappyWind() {
    try {
      if (lockSnowflakes.tryLock(10, TimeUnit.SECONDS)) {
        for (Snowflake snowflake : snowflakes) {
          happyWindSnowing.initializeSnowFlakeHappyWind(snowflake);
        }
        lockSnowflakes.unlock();
      } else {
        System.out.println("GUI thread is overloaded !!!");
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void switchDebug() {
    if (config.isDebug()) {
      pauseSnowing = true;
      try {
        if (lockSnowflakes.tryLock(10, TimeUnit.SECONDS)) {
          removeSnowflakes(snowflakes);
          this.generateNewSnowflake(20, 0.5);
          lockSnowflakes.unlock();
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    } else {
      pauseSnowing = false;
    }
  }

  @Override
  public void mouseMove(Point2D mouseLocation) {
  }

  @Override
  public void mouseScrolled(int count) {
    try {
      if (lockSnowflakes.tryLock(10, TimeUnit.SECONDS)) {
        for (Snowflake snowflake : snowflakes) {
          if (!snowflake.isFreezed()) {
            Point2D newLoc = snowflake.getLocation();
            newLoc.y += 10 * count;
            snowflake.setLocation(newLoc);
          }
        }
        lockSnowflakes.unlock();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void reset() {
    try {
      if (lockSnowflakes.tryLock(10, TimeUnit.SECONDS)) {
        removeSnowflakes(snowflakes);
        lockSnowflakes.unlock();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void init(FlagsConfiguration flagsConfiguration, Rectangle drawingSurface) {
    super.init(flagsConfiguration, drawingSurface);

    this.registerAttackLogic(new BigWormAttackStrategy(flagsConfiguration, getLogicController().getAudioPlayer()));
    this.registerAttackLogic(new DancingSnowflakesStrategy(flagsConfiguration, screenBounds, getLogicController().getAudioPlayer()));
    this.registerAttackLogic(new ParasitesAttackStrategy(flagsConfiguration, screenBounds, getLogicController().getAudioPlayer()));
    this.registerAttackLogic(new YinYangAttackStrategy(screenBounds, getLogicController().getAudioPlayer()));

    this.noWindSnowing = new NoWindSnowing(drawingSurface);
    this.normalWindSnowing = new NormalWindSnowing(drawingSurface);
    this.happyWindSnowing = new HappyWindSnowing();
  }

  private void registerAttackLogic(AttackStrategy<?> strategy) {
    if (this.attackStrategies.get(strategy.getAttackType()) != null) {
      throw new IllegalArgumentException("Ovveride strategy for attack!!");
    }
    this.attackStrategies.put(strategy.getAttackType(), strategy);
  }

  @Override
  public void mouseDown(int button, Point2D mouseLocation) {
    if (button == 1) {
      try {
        if (lockSnowflakes.tryLock(1, TimeUnit.SECONDS)) {
          for (int i = 0; i < 5; i++) {
            Snowflake snowflake = generateNewSnowflake();
            snowflake.setLocation(mouseLocation);
            snowflake.setSize(7);
            snowflake.setColor(new RGB(255, 140, 0));
            AttackStrategy<?> strategy = new FireworksStrategy(config, screenBounds, getLogicController().getAudioPlayer());
            strategy.beforeStart(List.of(snowflake));
            snowflake.setIndividualStrategy(strategy);
          }
          lockSnowflakes.unlock();
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    } else if (button == 3) {
      try {
        if (lockSnowflakes.tryLock(10, TimeUnit.SECONDS)) {
          Snowflake snowflake = generateNewSnowflake(50, 1);
          snowflake.setLocation(mouseLocation);
          snowflake.freeze();
          lockSnowflakes.unlock();
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void shutdown() {
    shutdown = true;
  }

  @Override
  public void switchAttack() {
    try {
      if (lockSnowflakes.tryLock(10, TimeUnit.SECONDS)) {
        if (config.isAttack()) {
          pauseSnowing = true;
          //Removed all freezed snoflakes
          removeSnowflakes(snowflakes.stream().filter(Snowflake::isFreezed).toList());
          this.cleanupSnowflakesData();
          this.getAttackStrategy().beforeStart(snowflakes);
        } else {
          pauseSnowing = false;
          this.cleanupSnowflakesData();
          this.getAttackStrategy().shutdown();
        }
        lockSnowflakes.unlock();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void changeAttackType(int oldType, int newType) {
    try {
      if (lockSnowflakes.tryLock(10, TimeUnit.SECONDS)) {
        AttackStrategy<?> strategy = attackStrategies.get(oldType);
        if (strategy != null) {
          strategy.shutdown();
        }
        this.cleanupSnowflakesData();
        this.getAttackStrategy().beforeStart(snowflakes);
      }
      lockSnowflakes.unlock();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void switchBlackHoles() {

  }

  @Override
  public void switchIndividualMovements() {
    try {
      if (lockSnowflakes.tryLock(10, TimeUnit.SECONDS)) {
        for (Snowflake snowflake : snowflakes) {
          if (!config.isIndividualMovements()) {
            snowflake.setIndividualStrategy(null);
            snowflake.cleanup();
            continue;
          }
          AttackStrategy<?> strategy = null;
          switch (((int) (Math.random() * 3))) {
            case 0 -> strategy = new BigWormAttackStrategy(config, getLogicController().getAudioPlayer());
            case 1 -> strategy = new DancingSnowflakesStrategy(config, screenBounds, getLogicController().getAudioPlayer());
            case 2 -> strategy = new ParasitesAttackStrategy(config, screenBounds, getLogicController().getAudioPlayer());
          }
          if (strategy != null) {
            snowflake.setIndividualStrategy(strategy);
            strategy.beforeStart(List.of(snowflake));
          }
        }
        lockSnowflakes.unlock();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    pauseSnowing = config.isIndividualMovements();
  }

  @Override
  public boolean canControllerStart() {
    return skipInitialAnimation || initialAnimation.isFinished();
  }

  @Override
  public void switchGraphicalSounds() {

  }

  private void cleanupSnowflakesData() {
    for (Snowflake snowflake : snowflakes) {
      snowflake.cleanup();
    }
  }

  @Override
  public void changedSnowingLevel() {
    try {
      if (config.getSnowingLevel() == 0) {
        pauseSnowing = true;
        if (lockSnowflakes.tryLock(10, TimeUnit.SECONDS)) {
          removeSnowflakes(snowflakes);
          lockSnowflakes.unlock();
        }
      } else {
        pauseSnowing = config.isAttack();
        this.resetSnowingTimer();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void provideImageData(ImageData imageData) {
    this.imageData = imageData;
  }

  private void checkCollisions() {
    try {
      if (imageData == null) {
        return;
      }
      if (lockSnowflakes.tryLock(2, TimeUnit.SECONDS)) {
        for (Snowflake snowflake : snowflakes) {
          if (snowflake.getIndividualStrategy() == null) {
            if (!snowflake.isFreezed()
                && !config.isIndividualMovements()
                && snowflake.getLocation().x > screenBounds.width / 2.0d - 150
                && snowflake.getLocation().x < screenBounds.width / 2.0d + 150
                && this.isColliding(snowflake, imageData)) {
              snowflake.freeze();
            }
          }
        }
        lockSnowflakes.unlock();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean isColliding(Snowflake snowflake, ImageData imageData) {
    final Set<RGB> colors = new HashSet<>();
    for (int x = -1; x < 2; x++) {
      Point screenLoc = GuiUtils.toScreenCoord((int) snowflake.getLocation().x + x,
          (int) snowflake.getLocation().y);
      colors.add(GuiUtils.getPixelColor(imageData, screenLoc.x, screenLoc.y));
    }
    return colors.stream().anyMatch(p -> p.equals(GuiUtils.FREEZE_COLOR));
  }

  public void skipInitialAnimation() {
    skipInitialAnimation = true;
  }

  private void resetSnowingTimer() {
    if (taskSnowing != null) {
      taskSnowing.cancel();
    }
    snowflakesTimeGen = (int) Utils.linearInterpolation(config.getSnowingLevel(), 1, 30, 10, 5);
    taskSnowing = new TimerTask() {
      @Override
      public void run() {
        if (!pauseSnowing && !getFlagsConfiguration().isPause()) {
          if (lockSnowflakes.tryLock()) {
            generateNewSnowflake();
            lockSnowflakes.unlock();
          }
        }
      }
    };
    timer.scheduleAtFixedRate(taskSnowing, 0, snowflakesTimeGen);
  }

  @Override
  protected int getSleepDuration() {
    return 2;
  }

  public List<Snowflake> getSnowflakes() {
    return snowflakes;
  }

  void addSnowflake(Snowflake snowflake) {
    this.snowflakes.add(snowflake);
  }

}
