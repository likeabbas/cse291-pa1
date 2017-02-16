docker build -t server ./server
docker build -t client ./client

docker run -d -i --name pingServer server /bin/bash -c "cd /usr/local/; java pingpong.server.PServer 0.0.0.0 3000"
ip=`docker inspect --format='{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' pingServer`
docker run -t -i --name pingClient --link pingServer client /bin/bash -c "export ip=$ip; java pingpong.client.PingClient 0.0.0.0 3000"

docker stop pingServer
docker stop pingClient

docker rm pingServer
docker rm pingClient
