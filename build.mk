.PHONY: all

clean:
	mvn clean package -DskipTests

kuber-crud-app-docker-build:
	docker build -t kuber-crud-app:latest -f kuber-crud-app/Dockerfile .

auth-service-docker-build:
	docker build -t auth-service:latest -f auth-service/Dockerfile .

all: clean kuber-crud-app-docker-build auth-service-docker-build
