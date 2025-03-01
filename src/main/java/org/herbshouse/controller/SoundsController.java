package org.herbshouse.controller;

import org.herbshouse.audio.AudioPlayer;

public interface SoundsController {

  void switchGraphicalSounds();

  void setGraphicalSound(GraphicalSoundConfig graphicalSoundConfig);

  void setAudio(AudioPlayer audioPlayer);

  void switchYoutube();

  void playNextYoutube();

}
