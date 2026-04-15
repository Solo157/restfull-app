# Устанавливаем внешний (не встроенный) ingress-nginx контроллер для внешних запросов.
install:
	helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx || true
	helm repo update
	helm install nginx ingress-nginx/ingress-nginx -f nginx-ingress/nginx-ingress.yaml --namespace auth-kuber-service-space --create-namespace

update:
	helm upgrade nginx ingress-nginx/ingress-nginx -f nginx-ingress/nginx-ingress.yaml --namespace auth-kuber-service-space
# 	helm uninstall nginx --namespace auth-kuber-service-space
# 	helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx || true
# 	helm repo update
# 	helm install nginx ingress-nginx/ingress-nginx -f nginx-ingress/nginx-ingress.yaml --namespace auth-kuber-service-space --create-namespace

# all-update:
#     @bash -c ' \
#         until helm uninstall nginx --namespace auth-kuber-service-space; do \
#             echo "Uninstall failed or nginx not installed, trying again..."; \
#             sleep 2; \
#         done; \
#         helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx || true; \
#         helm repo update; \
#         until helm install nginx ingress-nginx/ingress-nginx -f nginx-ingress/nginx-ingress.yaml --namespace auth-kuber-service-space --create-namespace; do \
#             echo "Install failed, retrying in 5 seconds..."; \
#             sleep 5; \
#         done'
