.PHONY: all clean-minikube-docker clean-minikube

# clean-minikube-docker:
# 	eval $$(minikube docker-env) && \
# 	docker ps -a --filter ancestor=grafana-kuber-app:latest -q | xargs -r docker rm -f && \
# 	docker rmi grafana-kuber-app:latest --force && \
# 	eval $$(minikube docker-env -u)

minikube-delete:
	minikube delete

all: minikube-delete
