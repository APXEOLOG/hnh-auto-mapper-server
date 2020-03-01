### Features
 - Minimap images are sent automatically from the client
 - Automatic zoom level generation
 - Live-mode available (you can see online characters moving on the map)
 - Ingame markers can also be automatically sent to the client (including quest givers and custom markers)
 - Geolocation available for the client
 - Metrics and server state can be monitored through the Humio
 - Good performance (easily handled ~40 clients sending updates at the same time at the W11 start)
 - Cheap maintenance (or even free - can be run inside free-tier t2.micro aws instance)
 - Optimal stability (worked nearly the whole W11 (a year) with only few reboots)

Some examples:
Live mode

![Live mode](https://webm.red/view/FrOT.webm)

Geolocation

![Geolocation](https://i.imgur.com/gtbNjre.png)

### Configuration and build

Detailed configuration options can be found inside backend/frontend folders

Deployment package can be generated using `create-distribution.sh` script

To config initial values open the script and edit variables at the top

```shell script
export DOMAIN_NAME="ec2-1-2-3.eu-central-1.compute.amazonaws.com"  # Domain for your server
export ENDPOINT_URL="http://ec2-1-2-3.eu-central-1.compute.amazonaws.com" # Endpoint for the frontend/api
export HUMIO_TOKEN="abcdef" # Humio token if present (see backend readme)
export MAP_USER="TestUser" # Username to login into the map
export MAP_PASSWORD="TestPassword" # Password to login into the map
export MAP_ADMIN_TOKEN="abcdef" # Admin token for map api
export INSTANCE_MAP_USER="mapuser" # Your VM user, which will be use to run map
```

After invocation of this script you will get `hnh-map` folder with all required data inside
 - `hnh-map/public` - built frontend
 - `hnh-map/map-backend.jar` - backend server jar
 - `hnh-map/application.properties` - backend server properties
 - `hnh-map/start.sh` - startup script
 - `hnh-map/nginx.conf` - nginx configuration (should replace default nginx.conf)
 - `hnh-map/hnhmap.service` - systemd service for the backend server
 - `hnh-map/install.sh` - install script
 
### Configuring VPS instance
 
You don't have very specific restriction about OS. I've tested everything with Ubuntu 18.x
 
Required software:
 - Java (tested with OpenJDK 11)
 - Nginx (tested with 1.14)
 - [PNGQuant](https://pngquant.org/) (tested with 2.12.5)
 - vsftpd (optional ftp server to upload distribution)
  
Installation process (for Ubuntu 18.x)
1. `sudo apt-get update`
2. `sudo apt install pngquant nginx default-jdk vsftpd` 
3. `sudo adduser mapuser` - Add new user for map server
4. `usermod -aG sudo mapuser` - Set as sudouser 
5. Configure vsftpd. You can take any guide, for example [DC](https://www.digitalocean.com/community/tutorials/how-to-set-up-vsftpd-for-a-user-s-directory-on-ubuntu-18-04)'s one
6. Upload `hnh-map` folder to the remove server (the structure should looks like this: `/home/mapuser/hnh-map/install.sh`)
7. `su - mapuser` - Login as mapuser
8. Move into `~/hnh-map/` folder and run `./install.sh`. Script should update nginx conf and systemd service

This should be it. Now try to access your instance on port 80 and you should see frontend application
