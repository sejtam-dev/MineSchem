# MineSchem
Add schematic support for minestom

Support
---
 - Sponge Schematic (1.13 - 1.16.4) ✅ - [Sponge](https://github.com/SpongePowered/Sponge/blob/api-8/invalid/main/java/org/spongepowered/common/world/schematic/SchematicTranslator.java)
    - Read ✅ - [WorldEdit](https://github.com/EngineHub/WorldEdit/blob/master/worldedit-core/src/main/java/com/sk89q/worldedit/extent/clipboard/io/SpongeSchematicReader.java)
    - Write ✅ - [WorldEdit](https://github.com/EngineHub/WorldEdit/blob/master/worldedit-core/src/main/java/com/sk89q/worldedit/extent/clipboard/io/SpongeSchematicWriter.java)
    - Build ✅
    
 - MCEdit Schematic (x.x - 1.12.2) ❌
    - Read ✅ - [WorldEdit](https://github.com/EngineHub/WorldEdit/blob/master/worldedit-core/src/main/java/com/sk89q/worldedit/extent/clipboard/io/MCEditSchematicReader.java)
    - Write ❌
    - Build ✅
    
Maven
---

Repository
```
<repositories>
    <repository>
         <id>MineSchem</id>
         <url>https://repo.repsy.io/mvn/sejtam10/mineschem</url>
    </repository>
</repositories>
```

Dependency
```
<dependency>
    <groupId>dev.sejtam</groupId>
    <artifactId>MineSchem-Core</artifactId>
    version>2.0.0</version>
</dependency>
```

Gradle
---

Repository
```
repositories {
    maven { url 'https://repo.repsy.io/mvn/sejtam10/mineschem' }
}
```

Dependency
```
dependencies {
    compile 'dev.sejtam:MineSchem-Core:2.0.0'
}
```


