# AQLFederationTool

## What is AQLFederationTool?

AQLFederationTool (AFT) distributes openEHR AQL queries to remote sites and merges the results.
Currently, the number of patients is returned for feasibility queries.


## Configuration

Remote sites and the local openEHR database can be configured in [application.yml](src/main/resources/application.yml).
Or the configuration can be copied to a new file and the file name passed as a command line argument: `--spring.config.additional-location=my-config.yml`.

```
aft:
  location: "Add your site name here"
  remote-locations:
    - name: "Other site"
      url: "https://aft.example.com"
    - name: "Yet another site"
      url: "https://asdf.example.net"
  openehr:
    base-url: http://localhost:38080/ehrbase/
    security:
      type: basic
      user:
        name: username
        password: "your secret password"
```
Alternatively, configuration values can be passed as environment variables. See the docker compose file for an example.

## Installation

Use the supplied [Dockerfile](Dockerfile) to create an AFT docker container.

Run `docker build --tag 'aft' && docker run -p 8081:8081/tcp 'aft'`.


## Testing/Development

Run AFT with `mvn spring-boot:run`.

The [Docker Compose file](docker/docker-compose.yml) can be used to run different combinations of "local/remote location" with Ehrbases and AFT.

Which services are started determined by the active profile:

- for testing
    - `docker compose -f docker/compose.yml --profile local --profile remote1 --profile remote2 up`
    - this runs one local location with AFT + Ehrbase and two remote locations with each AFTs + Ehrbases

- for development
    - this runs one local Ehrbase and one remote AFT + Ehrbase
        - `docker compose -f docker/docker-compose.yml --profile local-eb remote1 up`
