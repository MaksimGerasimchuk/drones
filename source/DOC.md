## Drones app docs

### Description

Here is a Java 17 + Spring Boot implementation of drones REST service.
It uses H2 (embedded) database, liquibase for schema migrations and Maven as a dependency management.

### Build and run instructions

Run following command from project root directory

```
./mvnv clean install
```

Then go to a /target directory and run following command (you should have JDK 17 installed):

```
java -jar drones-logistics-0.0.1-SNAPSHOT.jar
```

Then you should see a lot of log messages.
We're interested in ' Drones generated successfully' and 'Started DronesLogisticsApplication in ... seconds'
If we see these messages then our app has started and available on port 8080.

### Swagger

OpenAPI docs available at '/swagger-ui.html' path.

### Implementation details

1. App generated 10 drones at startup time.
   API designed to provide an ability to register new drones (and also remove existing).

2. App has several scheduled tasks to maintain drones and cargos state
   (and also util tasks for drones batteries charging/discharging emulation)
   For the sake of simplicity there was no any additional tuning performed, but
   for real cases at least some non-default thread pool should be set up.
   Also a lot of things has been simplified (in a real world scenario transitions between drone states
   can be more complicated, e.g. drone's battery become dead while it was delivering cargo)
   
3. There are unit tests examples in corresponding packages.
   The 100% coverage is not a purpose of this task.








