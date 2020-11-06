package de.fstab.demo.superrandom.extension;

import de.fstab.demo.SuperRandom;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class SuperRandomInitializer {

  static SuperRandom rand;

  public void initialize() {
    rand = new SuperRandom();
  }
}
