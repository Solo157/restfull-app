.PHONY: all prepare-docker-minikube install-test install-prod

prepare-docker-minikube:
	-kubectl create namespace auth-kuber-service-space || true
	eval $$(minikube docker-env) && \
	mvn -f auth-service/pom.xml clean package -DskipTests && \
	mvn -f billing-service/pom.xml clean package -DskipTests && \
	mvn -f notification-service/pom.xml clean package -DskipTests && \
	mvn -f order-service/pom.xml clean package -DskipTests && \
	docker build -t auth-service:latest -f auth-service/Dockerfile auth-service && \
	docker build -t billing-service:latest -f billing-service/Dockerfile billing-service && \
	docker build -t notification-service:latest -f notification-service/Dockerfile notification-service && \
	docker build -t order-service:latest -f order-service/Dockerfile order-service && \
	eval $$(minikube docker-env -u)

install-test: prepare-docker-minikube
# 	helm install auth-kuber-service-space ./mychart -f ./mychart/test-values.yaml -n auth-kuber-service-space

# install-prod: prepare-docker-minikube
# 	helm install auth-kuber-service-space ./mychart -f ./mychart/prod-values.yaml -n auth-kuber-service-space
#
# upgrade-test: prepare-docker-minikube
# 	helm uninstall auth-kuber-service-space -n auth-kuber-service-space || true
# 	helm install auth-kuber-service-space ./mychart -f ./mychart/test-values.yaml -n auth-kuber-service-space
#
# upgrade-prod: prepare-docker-minikube
# 	helm uninstall auth-kuber-service-space -n auth-kuber-service-space || true
# 	helm install auth-kuber-service-space ./mychart -f ./mychart/prod-values.yaml -n auth-kuber-service-space

