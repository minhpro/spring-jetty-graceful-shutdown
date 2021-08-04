## Build docker image

docker build -t spring-jetty-graceful-shutdown . 

## Using kind load the image

kind load docker-image spring-jetty-graceful-shutdown

## Deploy by using kubectl

kubectl apply -f deployment.yaml

kubectl apply -f service.yaml