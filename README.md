# restfull-app
Приложение - сервис заказа.

Приложение состоит из следующих компонентов/модулей:
- Auth-Service.
Сервис предназначен для регистрации и аутентификации пользоателя. При регистрации создается новый пользователь. 
Данный сервис хранит всех пользователей. При аутентификации пользователь получает токен, который используется для доступа
к API других микросервисов.
Auth сервис, при создании пользователя, также по REST запросу к Billing сервису создает аккаунт пользователя.
- Billing-Service.
Сервис предназначен для хранения пользовательских аккаунтов с их суммами. При создании заказа средства списываются с 
аккаунта пользователя.
- Order-Service.
Сервис предназначен для создания заказа пользователем. После создания заказа происходит обращение к сервису Billing
для снятия средств. При любом исходе: удалось снять или нет, отправляется ивент в данный Order сервис. Затем, 
заказ становится оплаченным и завершенным, либо неоплаченым и отмененным. В любом из вариантов отправляется ивент в сервис
нотификаций.
Order сервис проверяет, хватит ли средств на счете у Billing сервиса по REST запросу.
- Notificatin-Service.
Сервис предназначен для хранения информации по заказам пользователя. Принимает любые ивенты по заказу и сохраняет их с
определенным сообщением, указывающим что произошло с заказом. Пользователь может смотреть информацию по заказам.
- Брокер, RabbitMQ.
Брокер, с помощью которого компоненты/модули взаимодействуют между собой асинхронно. 

ЗАПУСК ПРИЛОЖЕНИЯ
Используется namespace: auth-kuber-service-space

Установка микросервисов/приложения:
Прежде чем выполнять установку, нужно сопоставить DNS и IP миникуба (это IP адрес ноды кубернетеса, устанавливается на 1 ноду)
Узнать IP кубера (будет IP по котором достучимся до nginx по 80 порту): kubectl get nodes -o wide
Изменить hosts: sudo nano /etc/hosts меняем на {IP_minikube} arch.homework
Для проверки работоспособности выполняем curl: curl -H "Host: arch.homework" http://{IP_minikube}/auth/health

Если нужно скомпилировать и запушить в docker registry:
1. registry прописать в главном pom и пароль установить в .m2/settings.xml
2. Компилируем микросервисы: mvn clean install
3. Пушим в докер registry: mvn jib:build
По умолчанию этого делать не нужно, т.к. образы берутся из github.

Как запустить:
1. Стартуем миникуб: minikube start
2. Создаем namespace: kubectl create namespace auth-kuber-service-space
3. Создаем/обновляем кофигурацию и секреты в кубернетес:
   helm upgrade --install common-config charts/common-config/ -f charts/common-config/test.yaml
   helm upgrade --install common-secret charts/common-secret/ -f charts/common-secret/test.yaml
4. Скачиваем и поднимаем ingress-controller: make -f install-nginx-ingress.mk install

helm upgrade --install nginx ingress-nginx/ingress-nginx \
-f nginx-ingress/nginx-ingress.yaml \
--namespace auth-kuber-service-space \
--create-namespace

Если поднять не получилось, то скорее всего проблема со скачиванием образа. Поэтому нужно его скачать и запушить в 
docker миникуба:
   docker pull registry.k8s.io/ingress-nginx/controller:v1.15.1@sha256:594ceea76b01c592858f803f9ff4d2cb40542cae2060410b2c95f75907d659e1
   minikube image load registry.k8s.io/ingress-nginx/controller:v1.15.1
5. С помощью команды kubectl get pods -n auth-kuber-service-space удостоверяемся, что ingress стал READY.
6. Создаем/поднимаем все сервисы:

   helm dependency build ./charts/auth && \
   helm dependency build ./charts/billing && \
   helm dependency build ./charts/notification && \
   helm dependency build ./charts/order && \
   helm dependency build ./charts/delivery && \
   helm dependency build ./charts/inventory && \
   helm dependency build ./charts/auth-validate
7. 
   helm upgrade --install auth-service ./charts/auth -n auth-kuber-service-space && \
   helm upgrade --install billing-service ./charts/billing -n auth-kuber-service-space && \
   helm upgrade --install notification-service ./charts/notification -n auth-kuber-service-space && \
   helm upgrade --install order-service ./charts/order -n auth-kuber-service-space && \
   helm upgrade --install delivery-service ./charts/delivery -n auth-kuber-service-space && \
   helm upgrade --install inventory-service ./charts/inventory -n auth-kuber-service-space && \
   helm upgrade --install rabbitmq-service ./charts/rabbit -f charts/rabbit/values.yaml -n auth-kuber-service-space && \
   helm upgrade --install auth-validate-service ./charts/auth-validate -f charts/auth-validate/values.yaml -n auth-kuber-service-space

ТЕСТИРОВАНИЕ:
Проверка через newman (заранее нужно установить):
newman run postman/RESTFULL.postman_collection.json -e postman/OtusEnvironment.postman_environment.json
