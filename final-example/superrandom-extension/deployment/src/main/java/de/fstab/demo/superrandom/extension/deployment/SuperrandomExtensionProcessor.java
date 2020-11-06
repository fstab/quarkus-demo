package de.fstab.demo.superrandom.extension.deployment;

import de.fstab.demo.SuperRandom;
import de.fstab.demo.superrandom.extension.SuperRandomInitializer;
import de.fstab.demo.superrandom.extension.SuperRandomSupplier;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.QuarkusClassWriter;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.BytecodeTransformerBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import javax.inject.Singleton;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;
import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

class SuperrandomExtensionProcessor {

    private static final String FEATURE = "superrandom-extension";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Record(RUNTIME_INIT)
    void init(SuperRandomInitializer recoder) {
        recoder.initialize();
    }

    @BuildStep
    BytecodeTransformerBuildItem makeBytecodeTransformer() {
        return new BytecodeTransformerBuildItem(SuperRandom.class.getName(),
            (className, inputClassVisitor) -> new ClassVisitor(Opcodes.ASM8, new QuarkusClassWriter(
                ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS)) {
              @Override
              public void visitEnd() {
                try {
                  super.visitEnd();
                  byte[] transformedBytecode = this.getClass().getClassLoader()
                      .getResourceAsStream("/SuperRandom.class").readAllBytes();
                  ClassReader cr = new ClassReader(transformedBytecode);
                  cr.accept(inputClassVisitor, 0);
                } catch (Exception e) {
                  e.printStackTrace();
                  System.exit(1);
                }
              }
            });
    }


    @BuildStep
    SyntheticBeanBuildItem makeCdiBean() {
        return SyntheticBeanBuildItem.configure(SuperRandom.class)
            .scope(Singleton.class)
            .supplier(new SuperRandomSupplier())
            .done();
    }

}
