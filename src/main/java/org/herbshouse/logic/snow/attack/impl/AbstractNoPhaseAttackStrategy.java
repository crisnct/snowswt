package org.herbshouse.logic.snow.attack.impl;

import java.util.List;
import org.herbshouse.audio.AudioPlayOrder;
import org.herbshouse.audio.AudioPlayType;
import org.herbshouse.audio.AudioPlayer;
import org.herbshouse.logic.snow.Snowflake;
import org.herbshouse.logic.snow.attack.AttackStrategy;
import org.herbshouse.logic.snow.data.AbstractAttackData;

public abstract class AbstractNoPhaseAttackStrategy<T extends AbstractAttackData> implements
    AttackStrategy<T> {

  private final AudioPlayer audioPlayer;
  private boolean started = false;

  public AbstractNoPhaseAttackStrategy(AudioPlayer audioPlayer) {
    this.audioPlayer = audioPlayer;
  }

  public void playAudio(String filename, AudioPlayType type, float volume) {
    if (audioPlayer != null && audioPlayer.isPlaying("sounds/" + filename)) {
      AudioPlayOrder order = new AudioPlayOrder("sounds/" + filename);
      order.setType(type);
      order.setVolume(volume);
      audioPlayer.play(order);
    }
  }

  public void stopAudio(String filename) {
    if (audioPlayer != null) {
      audioPlayer.stop("sounds/" + filename);
    }
  }

  @Override
  public void beforeStart(List<Snowflake> snowflakeList) {
    started = true;
  }

  @Override
  public abstract T getData(Snowflake snowflake);

  @Override
  public boolean isStarted() {
    return started;
  }

  @Override
  public boolean isFinished() {
    return false;
  }
}
