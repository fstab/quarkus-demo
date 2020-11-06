package de.fstab.demo.superrandom.extension;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import de.fstab.demo.SuperRandom;

@TargetClass(SuperRandom.class)
public final class Target_de_fstab_demo_SuperRandom {

  @Substitute
  public int nextInt() {
    return 42;
  }
}
