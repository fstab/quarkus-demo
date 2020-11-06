package de.fstab.demo;

import java.util.Random;

public class SuperRandom {

  private final Random random = new Random(System.currentTimeMillis());

  public int nextInt() {
    return random.nextInt();
  }
}
