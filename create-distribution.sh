export DOMAIN_NAME="ec2-1-2-3.eu-central-1.compute.amazonaws.com"
export ENDPOINT_URL="http://ec2-1-2-3.eu-central-1.compute.amazonaws.com"
export HUMIO_TOKEN="abcd"
export MAP_USER="TestUser"
export MAP_PASSWORD="TestPassword"
export MAP_ADMIN_TOKEN="abcd"
export INSTANCE_MAP_USER="mapuser"

# Create package folder
rm -rf hnh-map
mkdir hnh-map

# Build backend
cd map-backend/
sh gradlew bootJar
cd ../

# Copy jar
cp map-backend/build/libs/map-backend.jar hnh-map/map-backend.jar

# Change frontend config.js
cat <<EOF > map-frontend/src/config.js
export const MAP_ENDPOINT = '$ENDPOINT_URL';
EOF

## Build frontend
cd map-frontend
npm isntall
npm run build
cd ../

# Copy output
mkdir hnh-map/public
cp -r map-frontend/dist/* hnh-map/public

# Create backend configuration file
cat <<EOF > hnh-map/application.properties
management.metrics.export.humio.apiToken=$HUMIO_TOKEN
management.metrics.export.humio.tags.type=hnhmap

map.user.login=$MAP_USER
map.user.password=$MAP_PASSWORD
map.admin.token=$MAP_ADMIN_TOKEN
EOF

# Create nginx.config file
cat <<EOF > hnh-map/nginx.conf
user $INSTANCE_MAP_USER;
events {
  worker_connections  1024;
}
http {
  include mime.types;

  server {
    listen 80;
    server_name $DOMAIN_NAME;

    client_max_body_size 1M;

    gzip on;
    gzip_comp_level 4;
    gzip_types application/json;

    location / {
      root /home/$INSTANCE_MAP_USER/hnh-map/public;
      index index.html;
      access_log off;
    }

    location /api/ {
      proxy_pass http://127.0.0.1:5000;
      proxy_http_version 1.1;
      proxy_set_header Host \$host;
      proxy_set_header X-Real-IP \$remote_addr;
      proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    }
  }
}
EOF

# Create start.sh
cat <<EOF > hnh-map/start.sh
java -jar /home/$INSTANCE_MAP_USER/hnh-map/map-backend.jar
EOF

# Create hnhmap.service
cat <<EOF > hnh-map/hnhmap.service
[Unit]
Description=HnH Map Backend
After=network.target

[Service]
User=$INSTANCE_MAP_USER
ExecStart=/bin/bash /home/$INSTANCE_MAP_USER/hnh-map/start.sh
WorkingDirectory=/home/$INSTANCE_MAP_USER/hnh-map
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF

# Finally create install script
cat <<EOF > hnh-map/install.sh
# Give rw permissions for this folder, so nginx will be happy
sudo chmod -R 766 ./

# Stop map service if installed
sudo systemctl stop hnhmap

# Copy service file
sudo cp hnhmap.service /etc/systemd/system/hnhmap.service

# Reload daemon
sudo systemctl daemon-reload

# Enable backend service
sudo systemctl enable hnhmap
sudo systemctl start hnhmap

# Replace nginx.conf
sudo cp -f nginx.conf /etc/nginx/nginx.conf

# Restart nginx
sudo systemctl restart nginx
EOF
