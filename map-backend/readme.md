### Requirements

Not much, just JDK (11+)

On Linux you will also need to install [PNGQuant](https://pngquant.org/), it is used to optimize minimap images

### Build

Run `./gradlew bootJar`

Complete 'fat' jar with all dependencies will be placed at `build/libs/map-backend.jar`

### Run

Run `java -jar map-backend.jar`

### Configuration

Rename `application.properties.example` into `application.properties` and configure values inside

#### Authentication

Map user login and password are used to authenticate frontend app (this is what you will put into login form)

Admin token is used for special admin APIs, do not expose it to the other users

```
map.user.login=Kappa
map.user.password=Pride
map.admin.token=asdasdasd 
```

#### Humio metrics tracking (https://www.humio.com/)

You can setup metrics tracking for the backend, to check load / online / other stats 

Register account on Humio, create Ingest API Token and put it into configuration

```
management.metrics.export.humio.apiToken=XXX
management.metrics.export.humio.tags.type=XXX
```

You can remove this two lines from config if you don't want to use Humio
