# Maven Search CLI

A command-line tool to search for dependencies in Maven Central and generate dependency declarations for various build tools.

## Features

- Search Maven Central repository
- View latest versions of artifacts
- Search for older versions
- Copy dependency declarations to clipboard
- Support for multiple dependency formats:
  - Maven
  - Gradle (Kotlin DSL)
  - Gradle (Groovy DSL)
  - SBT

## Building

```bash
./mvnw clean package

java -jar target/mvn-search-0.1.0-SNAPSHOT-jar-with-dependencies.jar "spring-boot-starter-parent"
java -jar target/mvn-search-0.1.0-SNAPSHOT-jar-with-dependencies.jar "hibernate-validator"
java -jar target/mvn-search-0.1.0-SNAPSHOT-jar-with-dependencies.jar "g:org.slf4j"
java -jar target/mvn-search-0.1.0-SNAPSHOT-jar-with-dependencies.jar "junit" -f gradle
```

## References

- https://github.com/erosb/mvn-search
