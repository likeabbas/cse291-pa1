docker build -t client  -f Dockerfile.client .
docker build -t server  -f Dockerfile.server .
docker run -d --name=server server
HOST="$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' server)"
docker run --name=client  client \
java pingpong.client.PingClient $HOST 8000
docker stop client
docker stop server
docker rm client
docker rm server
