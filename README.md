# NMSMapper

[![Build Status](https://ci.screamingsandals.org/job/NMSMapper/badge/icon)](https://ci.screamingsandals.org/job/NMSMapper/)
[![Repository version](https://img.shields.io/nexus/r/org.screamingsandals.nms/NMSMapper?server=https%3A%2F%2Frepo.screamingsandals.org)](https://repo.screamingsandals.org/#browse/browse:maven-releases:org%2Fscreamingsandals%2Fnms%2FNMSMapper)
[![Discord](https://img.shields.io/discord/582271436845219842?logo=discord)](https://discord.gg/4xB54Ts)

This library will be used to easily get any information about nms classes and should speed up updates of our plugins when new Minecraft version is released!

> Mappings generated by this library can be browsed [here](https://nms.screamingsandals.org/)  
>
> It's possible that these mappings contains mistakes in the history tab. We are only solving issues in classes we are using. If you want to fix anything, feel free to open pull request or contact us on our discord server.
>
> NMSMapper is made for servers! Many client-side mappings are missing.

## Features and planned features

- [X] Reading Mojang obfuscation maps
- [X] Reading Spigot mappings
- [X] Reading MCP mappings
- [ ] Reading Yarn mappings
- [X] Exporting result for each version to separated json file
- [X] Support for pre-obfuscation map versions
- [X] Generating website where you can easily browse through mapping (just like javadocs)
- [X] Getting extra information about classes (modifiers, superclass, implemented interfaces) - server jar needed
- [X] Getting extra information about fields (type)
- [X] Getting extra information about fields (modifiers) - server jar needed
- [X] Getting extra information about methods (return type)
- [X] Getting extra information about methods (modifiers) - server jar needed
- [X] Comparing between versions, saving result to file (file will be then used by slib to generate reflection)
- [ ] Caching for json files to speed up building
- [X] Using NMSMapper to generate accessors

## Usage

> Note: This project requires Gradle >= 7.0. Maven is not supported.  
> For compilation at least JDK 11 is required. However the compiled classes use only Java 8 methods.

* Load NMSMapper as gradle plugin
  ```groovy
  buildscript {
      repositories {
          maven { url 'https://repo.screamingsandals.org/public/' }
      }
      dependencies {
          classpath 'org.screamingsandals.nms:NMSMapper:1.0.1-SNAPSHOT'
      }
  }

  apply plugin: 'org.screamingsandals.nms'
  ```
* Setup NMSMapper
  ```groovy
  /* First add new source set. Don't use your main source set for generated stuff. */
  sourceSets.main.java.srcDirs = ['src/generated/java', 'src/main/java']

  /* All other things will be set inside nmsGen method */
  nmsGen {
      basePackage = "com.example.nms.accessors" // All generated classes will be in this package.
      sourceSet = "src/generated/java" // All generated classes will be part of this source set.
    
      /* This means that the folder will be cleared before generation. 
       *
       * If this value is false, old no longer used classes won't be removed.
       */
      cleanOnRebuild = true 
    
      /* Here we can define the classes */
  }
  ```
* Define what classes, fields and methods will be used
  ```groovy
  reqClass('net.minecraft.core.Rotations')
  ```
  We want to access Rotations in our plugin. This method generates new class `RotationsAccessor` which you can use to retrieve the type  
  The generated code looks like this:
  ```java
  public class RotationsAccessor {
  
    public static Class<?> getType() {
      return AccessorUtils.getType(RotationsAccessor.class, mapper -> {
          mapper.map("spigot", "1.9.4", "net.minecraft.server.${V}.Vector3f");
          mapper.map("spigot", "1.17", "net.minecraft.core.Vector3f");
          mapper.map("mcp", "1.9.4", "net.minecraft.util.math.Rotations");
          mapper.map("mcp", "1.17", "net.minecraft.src.C_4709_");
        });
    }
    
  }
  ```
  We can see we got new static method `getType()` which returns class based on the version and platform (Spigot and Forge is supported)
  
  Okay, we have class. But classes are not all, we also need to access some fields, methods or even constructors.
  ```groovy
  reqClass('net.minecraft.core.Rotations')
        .reqConstructor(float, float, float)
        .reqField('x')
        .reqField('y')
        .reqField('z')
        .reqMethod('getX')
        .reqMethod('getY')
        .reqMethod('getZ')
  ```
  This will generate access methods for one constructor, three fields and three methods
  ```java
  public class RotationsAccessor {
    public static Class<?> getType() {
    }

    public static Field getFieldX() {
    }

    public static Field getFieldY() {
    }

    public static Field getFieldZ() {
    }

    public static Constructor<?> getConstructor0() {
    }

    public static Method getMethodGetX1() {
    }

    public static Method getMethodGetY1() {
    }

    public static Method getMethodGetZ1() {
    }
  }
  ```
  Generated method for field will always be called getField<Name> and will return Field  
  Generated method for constructor will always be called getConstructor<Index> and will return Constructor<?>. This index is generated from the specified order in   build.gradle  
  Generated method for method will always be called getMethod<Name><Index> and will return Method. Here the index is present because multiple methods can have other parameters but same name

  > Note: If Field, Constructor<?>, Method or Class<?> is not found, `null` is returned

  Maybe you are asking: How to define parameters to methods? It's actually pretty easy and the same applies to constructors:
  ```groovy
  var Level = reqClass('net.minecraft.world.level.Level')

  reqClass('net.minecraft.world.entity.decoration.ArmorStand')
        .reqConstructor(Level, double, double, double)
        .reqMethod('setSmall', boolean)
  ```
  parameters can be classes `String.class` (in groovy you don't have to specify .class suffix), strings `java.lang.String` or another requested class (in this example it's Level)

  > You can also specify another nms class as parameter without requesting it, in this case you will have to add prefix `&`:  
  > `.reqConstructor('&net.minecraft.world.level.Level', double, double, double)`  
  > However the accessor for Level will be generated anyways

  If the class is enum and we want to retrieve its enum value, we can simply use `reqEnumField` method
  ```groovy
  reqClass('net.minecraft.network.protocol.game.ServerboundClientCommandPacket$Action')
     .reqEnumField('PERFORM_RESPAWN')
  ```
  In this case, getField method will be generated again, however it will return directly the Object instead of Field
  ```java
  public static Object getFieldPERFORM_RESPAWN() {
  }
  ```
* For generating accessor classes you will have to execute `generateNmsComponents` task

### Using alternative mappings

#### Classes

For classes we can use spigot mappings instead of mojang one. We are not using package in this case:
```groovy
reqClass('spigot:PacketPlayInClientCommand$EnumClientCommand')
```
Generated accessor will be called `PacketPlayInClientCommand_i_EnumClientCommandAccessor` instead of `ServerboundClientCommandPacket_i_Action`

#### Methods, Fields, Enum values

For these symbols we can use any mapping supported by NMSMapper (`mojang`, `searge`, `spigot`, `obfuscated`)  
If mapping is not specified, then Mojang mapping is used.
```groovy
reqClass('spigot:EntityLiving')
    .reqMethod('spigot:getAttributeInstance', IAttribute)
    .reqMethod('spigot:getAttributeMap')
    .reqMethod('spigot:getCombatTracker')
```

Methods generated with alternative mappings will also use alternative mappings in their names.

> You can also specify which version of mappings have to be used for finding field, method or enum value
> 
> ```groovy
>    reqClass('spigot:World')
>       .reqField('spigot:methodProfiler:1.9.4')
> ```

## Contributing

### How to add new minecraft version or update its info.json

```
$ ./gradlew generateNmsConfig -PminecraftVersion=1.18
```
