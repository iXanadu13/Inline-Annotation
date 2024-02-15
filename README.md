# Inline-Annotation

Provide annotation `@Inline` and `@InlineAt` to inline Java code, which supports JDK1.8+.

Utilize [JSR-269](https://jcp.org/en/jsr/detail?id=269) to process Abstract Syntax Tree (AST) before bytecode generating.

For study only, please **consider carefully** before applying it to actual production environments.

## Setup
### step 1

Use JDK1.8 or above for your project. You can download OpenJDK [here](https://www.oracle.com/java/technologies/downloads/).

Tested version: JDK1.8, JDK17 and JDK18.

### step 2

Deploy `InlineAnnotation-Processor` to your local repository. You can choose **one of** the options:
* Download `InlineAnnotation-Processor.jar` from release and then execute `mvn install:install-file -Dfile=InlineAnnotation-Processor-1.0-dev.jar -DgroupId=com.github.iXanadu13 -DartifactId=InlineAnnotation-Processor -Dversion=1.0-dev -Dpackaging=jar`
* Clone this project to local and run `mvn clean compile package deploy`

### step 3

Download `InlineAnnotation-Annotation.jar` from release and add dependency for it.

```xml
<dependency>
    <groupId>com.github.iXanadu13</groupId>
    <artifactId>InlineAnnotation-Annotation</artifactId>
    <version>1.0-dev</version>
    <scope>provided</scope>
</dependency>
```

Then add configuration to your maven-compiler-plugin.

Here is an example for JDK17:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.8.1</version>
    <configuration>
        <!-- Set to what you prefer -->
        <source>8</source>
        <!-- Set to what you prefer -->
        <target>8</target>
        <fork>true</fork>
        <!-- compilerArgs is not required if you use JDK1.8 -->
        <compilerArgs>
            <arg>-J--add-opens=jdk.compiler/com.sun.source.util=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>
            <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED</arg>
        </compilerArgs>
        <encoding>UTF-8</encoding>
        <annotationProcessorPaths>
            <path>
                <!-- you need to deploy InlineAnnotation-Processor to your local repository first -->
                <groupId>com.github.iXanadu13</groupId>
                <artifactId>InlineAnnotation-Processor</artifactId>
                <version>1.0-dev</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```
* `<compilerArgs>` is not required if you use JDK1.8.
* Don't forget to `enable annotation processing` if you use an IDE (Eclipse, IntelliJ IDEA etc.) to manage your project. For IntelliJ IDEA: `File > Settings > Build, Execution, Deployment > Compiler > Annotation Processors > Enable annotation processing`.
* You need to deploy module `InlineAnnotation-Processor` to your local repository first. (step 2)

## How To Compile Sources
### step 1

Because of the usage of the unofficial api, [jdk1.8](https://www.oracle.com/java/technologies/downloads/#java8) is required to build this project.

### step 2

Once import it as maven project, the dependency below in pom.xml cannot be found in central maven repo:
```xml
<dependency>
    <groupId>com.sun</groupId>
    <artifactId>tools</artifactId>
    <version>1.8</version>
    <scope>provided</scope>
</dependency>
```
This file can be found at `%JAVA_HOME%\lib\tools.jar`. 

You can use the command below to install it to your local maven repository:

`mvn install:install-file -Dfile=%JAVA_HOME%\lib\tools.jar -DgroupId=com.sun -DartifactId=tools -Dversion=1.8 -Dpackaging=jar`

**Or** use system dependency and copy the file to corresponding path:

```xml
<dependency>
    <groupId>com.sun</groupId>
    <artifactId>tools</artifactId>
    <version>1.8</version>
    <scope>system</scope>
    <systemPath>${basedir}/lib/tools.jar</systemPath>
</dependency>
```

### step 3

Run `mvn clean compile package` and see the output file at `Inline-Annotation\Example\target`.



## Some Example


## Limitation
* Because of the limitation of AST, it's hard to locate method declarations from method calls precisely, maybe [JavaParser](https://github.com/javaparser/javaparser) can do it better.
* If you want to inline a method with void return type `@InlineAt Object inline = AnnotationTest.doSomething();`, your IDE will complain a syntax error but it can be compile correctly. IDE plugin is needed to suppress it or maybe there is a better way to design `@InlineAt`.
* `@Inline` nested declaration is not supported yet.
* Only static method can be inlined currently.
