#!/bin/sh

cat <<EOF > ./application.properties
server.port = 5000
management.metrics.export.humio.apiToken=$HUMIO_TOKEN
management.metrics.export.humio.tags.type=hnhmap

map.user.login=$MAP_USER
map.user.password=$MAP_PASSWORD
map.admin.token=$MAP_ADMIN_TOKEN
EOF

java -jar ./map-backend.jar -Duser.home /hnh