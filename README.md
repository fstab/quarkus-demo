Quarkus Magic
=============

Example code for my talk _Quarkus Zauberei: Code manipulieren mit Quarkus Extensions_.

Talk Outline
------------

### Preparation

Open Intellij Projects

* `super-random`
* `quarkus` (for reference)

Open Web Sites

* [https://code.quarkus.io](https://code.quarkus.io)

### Getting Started

* Show [https://code.quarkus.io](https://code.quarkus.io)
* Select _RESTEasy JAX-RS_
* Download as ZIP

```
unzip ~/Downloads/code-with-quarkus.zip`
cd code-with-quarkus
./mvnw clean package -Pnative
```

While compiling, show code.

### Library vs. Extension

Add the `super-random` library as a dependency

```xml
<dependency>
    <groupId>de.fstab.demo</groupId>
    <artifactId>super-random</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

Use the library in the `ExampleResource` class:

```java
@Path("/random")
public class ExampleResource {

    SuperRandom random = new SuperRandom();

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String random() {
        return "your random number is " + random.nextInt() + "\n";
    }
}
```

Fix test

```java
@Test
public void testHelloEndpoint() {
    given()
      .when().get("/random")
      .then()
         .statusCode(200)
         .body(containsString("random"));
}
```

Modify `SuperRandom` to make the library incompatible with native mode:

```java
  static Thread t;

  static {
    t = new Thread(() -> {
      while (true) {
        try {
          System.out.println("hallo");
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
      }
    });
    t.start();
  }

  public int nextInt() {
    System.out.println("t = " + t);
    return random.nextInt();
  }
```

Compile to native image.

While compiling, talk about differences between Java and "Java that works in SubstrateVM".

### Extension

Create a new extension project with `./create-extension.sh`:

```
mvn io.quarkus:quarkus-maven-plugin:1.9.2.Final:create-extension -N \
    -DgroupId=de.fstab.demo \
    -DartifactId=superrandom-extension \
    -Dversion=1.0-SNAPSHOT \
    -Dquarkus.artifactIdBase=superrandom-extension \
    -Dquarkus.artifactIdPrefix=superrandom- \
    -Dquarkus.nameBase="Super Random"
```

Add the example library as a runtime dependency:

```xml
<dependency>
    <groupId>de.fstab.demo</groupId>
    <artifactId>super-random</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

In `code-with-quarkus`, in `ExampleResource`, replace `new` with `@Inject`:

```java
@Inject
SuperRandom random;
```

In the extension's `SuperRandomExtensionProcessor`, create a synthetic CDI bean:

```java
@BuildStep
SyntheticBeanBuildItem syntheticBean() {
    return SyntheticBeanBuildItem.configure(SuperRandom.class)
        .scope(Singleton.class)
        .supplier(new SuperRandomSupplier())
        .done();
}
```

Add arc-deployment as a deployment dependency:

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-arc-deployment</artifactId>
</dependency>
```

Add the `SuperRandomSupplier` to the `runtime` module:

```java
public class SuperRandomSupplier implements Supplier<SuperRandom> {

  @Override
  public SuperRandom get() {
    return SuperRandomInitializer.get();
  }
}
```

Add the `superrandom-extension` to `code-with-quarkus` (using `./add-extension.sh`)

```
./mvnw quarkus:add-extension \
    -Dextensions=de.fstab.demo:superrandom-extension:1.0-SNAPSHOT
```

Compile and run `code-with-quarkus`. Then

```
curl http://localhost:8080/random
```

### Runtime vs Deployment Time Initialization

Create a `SuperRandomInitializer` in the extension's `runtime` module:

```java
@Recorder
public class SuperRandomInitializer {

  private static SuperRandom rand;

  public void initialize() {
    rand = new SuperRandom();
  }
}
```

For the `@Recorder` annotation the `runtime` module needs the `quarkus-core` dependency:

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-core</artifactId>
</dependency>
```

Modify the `SuperRandomSupplier` to use the `SuperRandomInitializer`:

```java
@Override
public SuperRandom get() {
  return SuperRandomInitializer.rand;
}
```

Create a build step for calling the initializer:

```java
@BuildStep
@Record(STATIC_INIT)
void init(SuperRandomInitializer recorder) {
  recorder.initialize();
}
```

While compiling to a native executable, show the `@Recorder` JavaDoc and explain what's happenging. Later replace `STATIC_INIT` with `RUNTIME_INIT`.

### Quarkus Byte Code Transform

Create a build step as follows (using live template `asm`):

```java
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
```

Copy the pre-defined modified `SuperRandom.class`

```
cp SuperRandom.class superrandom-extension/deployment/src/main/resources/
```

While compiling to native mode, show the contents of the `target/` directory.


### SubstrateVM Byte Code Transformation

Create a SVM substitution in the `runtime` module:

```java
@TargetClass(SuperRandom.class)
public final class Target_de_fstab_demo_SuperRandom {

  @Substitute
  public int nextInt() {
    return 42;
  }
}
```

Add SVM as a Maven dependency to the `runtime` module:

```xml
<dependency>
    <groupId>org.graalvm.nativeimage</groupId>
    <artifactId>svm</artifactId>
</dependency>
```

While compiling to native mode, show more examples of SVM annotations.

### Summary and Outlook

[https://quarkus.io/guides/writing-extensions](https://quarkus.io/guides/writing-extensions)
