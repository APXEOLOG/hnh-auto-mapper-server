FROM node:lts-alpine as build-stage

WORKDIR /app

COPY package*.json ./
RUN npm install
COPY . .

RUN npm run build 


FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
ENV ENDPOINT_URL="http://ec2-1-2-3.eu-central-1.compute.amazonaws.com"
RUN mkdir /usr/share/nginx/mountpoint
VOLUME /usr/share/nginx/mountpoint
EXPOSE 80

RUN ln -s /usr/share/nginx/mountpoint/grids /usr/share/nginx/html/grids
COPY entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/bin/sh", "entrypoint.sh"]