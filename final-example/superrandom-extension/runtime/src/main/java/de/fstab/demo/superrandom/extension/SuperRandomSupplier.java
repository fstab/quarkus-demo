package de.fstab.demo.superrandom.extension;

import de.fstab.demo.SuperRandom;

import java.util.function.Supplier;

public class SuperRandomSupplier implements Supplier<SuperRandom> {
  @Override
  public SuperRandom get() {
    return SuperRandomInitializer.rand;
  }
}
