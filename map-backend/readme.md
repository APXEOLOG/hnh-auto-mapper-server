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

### Using Admin API to manage map

#### Cleanup all data from the map
`GET <map_endpoint>/api/v1/cleanup?token=<admin_token>`

#### Set zero grid (0; 0), which will be used as a starting point to accept new minimaps
This is required to get map working. You need to define (0; 0) grid so it will be able to accept minimaps from sessions which can be connected to the (0; 0)

`GET <map_endpoint>/api/v1/setZeroGrid?gridId=<grid_id>&token=<admin_token>`

#### Set zero grid (0; 0), which will be used as a starting point to accept new minimaps
Sometimes you can receive bad minimap (for example cave instead of surface). You can remove it using this api call

`GET <map_endpoint>/api/v1/removeGrid?x=<map_grid_x>&y=<map_grid_y>&token=<admin_token>`