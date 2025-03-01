package org.herbshouse;

import org.herbshouse.controller.DefaultControllerImpl;
import org.herbshouse.logic.UserInfo;
import org.herbshouse.logic.Utils;
import org.herbshouse.logic.enemies.EnemyGenerator;
import org.herbshouse.logic.snow.SnowGenerator;

public class Tests {

  public static void main(String[] args) {
    UserInfo userInfo = new UserInfo();

    SnowGenerator snowGenerator = new SnowGenerator();
    snowGenerator.skipInitialAnimation();

    EnemyGenerator enemyGenerator = new EnemyGenerator();

    DefaultControllerImpl controller = new DefaultControllerImpl();
    controller.setUserInfo(userInfo);
    controller.setDesiredFPS(60);
    controller.registerGenerator(snowGenerator);
    controller.registerGenerator(enemyGenerator);

    snowGenerator.start();
    enemyGenerator.start();

    Utils.sleep(10000);

    controller.shutdown();
    snowGenerator.shutdown();
    enemyGenerator.shutdown();
  }

}
