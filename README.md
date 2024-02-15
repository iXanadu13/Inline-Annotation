# Inline-Annotation

Provide annotation `@Inline` and `@InlineAt` to inline Java code.

Utilize [JSR-269](https://jcp.org/en/jsr/detail?id=269) to process Abstract Syntax Tree (AST) before bytecode generating.

For study only, please **consider carefully** before applying it to actual production environments.

## Setup
### step 1

Because of the usage of the unofficial api, [jdk1.8](https://www.oracle.com/java/technologies/downloads/#java8) is required to build this project.

### step 2

Once import it as maven project, the dependency below is missing in pom.xml.
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
* If you want to inline a method with void return type `@InlineAt Object inline = AnnotationTest.doSomething();`, your IDE will complain syntax error but it can be compile correctly. IDE plugin is needed to suppress it or maybe there is a better way to design `@InlineAt`.
* `@Inline` nested declaration is not supported yet.

