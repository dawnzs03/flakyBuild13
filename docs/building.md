## Building from source

Ensure you have JDK 17 (or newer) and Git installed

    java -version
    git --version

Instead of using a locally installed Maven, call the Maven wrapper script `mvnw` in the main folder of the project.
This will use the Maven version which is supported by this project.

---    
First clone the Keycloak repository:
    
    git clone https://github.com/keycloak/keycloak.git
    cd keycloak
    
To build Keycloak run:

    ./mvnw clean install
    
This will build all modules and run the testsuite.

To build Keycloak with adapters run:

    ./mvnw clean install -Pdistribution

To build only the server run:

    ./mvnw -pl quarkus/deployment,quarkus/dist -am -DskipTests clean install

You can then find the ZIP distribution in `quarkus/dist/target` folder.

---
**NOTE**

Classes from `org.keycloak.testsuite.*` packages aren't suitable to be used in production.

---

This project contains opt-in support for incremental Maven builds using the [Apache Maven Build Cache Extension](https://github.com/apache/maven-build-cache-extension) which is disabled by default.

To enable it for a single Maven command execution, enable it as follows: 

    ./mvnw -D -Dmaven.build.cache.enabled=true ...

To enable it by default, add it to the `MAVEN_OPTS` environment variable:

    export MAVEN_OPTS="-Dmaven.build.cache.enabled=true"

### Starting Keycloak

To start Keycloak during development first build as specified above, then run:

    java -jar quarkus/server/target/lib/quarkus-run.jar start-dev

To stop the server press `Ctrl + C`.

For more details, follow the [`quarkus` module documentation](../quarkus/README.md).

## Working with the codebase

We don't currently enforce a code style in Keycloak, but a good reference is the code style used by WildFly. This can be 
retrieved from [Wildfly ide-configs](https://github.com/wildfly/wildfly-core/tree/main/ide-configs).To import formatting 
rules, see following [instructions](http://community.jboss.org/wiki/ImportFormattingRules).

If your changes require updates to the database read [Updating Database Schema](updating-database-schema.md).

If your changes require introducing new dependencies or updating dependency versions please discuss this first on the
dev mailing list. We do not accept new dependencies to be added lightly, so try to use what is available.

### Building project from the IDE

Some parts of the project rely on generated code using Maven plugins. These steps might be skipped when building using
IDE resulting in compilation errors. To work around this make sure to build the project first using Maven. After the
initial build with Maven you should be able to build the project using the IDE as it will use the classes previously
generated by Maven plugins. Make sure you don't rebuild the whole project using the IDE as it would delete the generated
classes. E.g. in IntelliJ IDEA use `Build → Build Project` instead of `Build → Rebuild Project`.

---
**NOTE**

If you are building the Operator from your IDE, make sure to build the project with the `operator` profile enabled in Maven
as it's excluded by default:

    ./mvnw clean install -Poperator -DskipTests

---