# Устанавливаем внешний (не встроенный) ingress-nginx контроллер для внешних запросов.
all:
	helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx || true
	helm repo update
	helm install nginx ingress-nginx/ingress-nginx -f manifests/nginx-ingress.yaml
