docker build -t client  -f Dockerfile.client .
docker build -t server  -f Dockerfile.server .
docker run -d server
docker run client
java pingpong.client.PingClient
docker stop client
docker stop server
docker rm client
docker rm server
