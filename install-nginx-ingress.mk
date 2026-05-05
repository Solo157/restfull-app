# Устанавливаем внешний (не встроенный) ingress-nginx контроллер для внешних запросов.
install:
	helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx || true
	helm repo update
	helm install nginx ingress-nginx/ingress-nginx -f nginx-ingress/nginx-ingress.yaml --namespace auth-kuber-service-space --create-namespace