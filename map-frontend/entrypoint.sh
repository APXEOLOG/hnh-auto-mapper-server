#!/bin/sh


cat <<EOF > /usr/share/nginx/html/config.json
{
    "MAP_ENDPOINT": "$ENDPOINT_URL"
}
EOF

nginx -g "daemon off;"