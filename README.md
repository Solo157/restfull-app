# restfull-app

Приложение - сервис создания и доставки заказа.

Приложение состоит из следующих сервисов:
- Auth-Service.
  Сервис предназначен для регистрации и аутентификации пользоателя. При регистрации создается новый пользователь.
  Данный сервис хранит всех пользователей. При аутентификации пользователь получает токен, который используется для
  доступа
  к API других микросервисов.
  Auth сервис, при создании пользователя, также по REST запросу к Billing сервису создает аккаунт пользователя.
- Billing-Service.
  Сервис предназначен для хранения пользовательских аккаунтов с их суммами. При создании заказа средства списываются с
  аккаунта пользователя.
- Order-Service.
  Сервис предназначен для создания заказа пользователем. Создание заказа основано на использовании паттерна SAGA.
  Создание заказа происходит в 4 этапа:
1. Списываются средства в Billing-Service за заказ в аккаунте пользователя.
2. Резервируется товар на складе, в сервисе inventory-service.
3. Резервируется курьер в сервисе delivery-service.
4. Отправляется ивент в сервис notification-service об успешном завершении/создании заказа.
   В случае, если что-то пошло не так, происходит откат - выполняются компенсирующие транзакции.

- Inventory-Service.
  Сервис-склад в котором хранятся товары, их название и количество.
- Delivery-Service.
  Сервис доставки в котором находятся курьеры, которые могут развозить заказ.
- Notificatin-Service.
  Сервис предназначен для хранения информации по заказам пользователя. Принимает любые ивенты по заказу и сохраняет их с
  определенным сообщением, указывающим что произошло с заказом. Пользователь может смотреть информацию по заказам.
- Брокер, RabbitMQ.
  Брокер, с помощью которого компоненты/модули взаимодействуют между собой асинхронно.

Идемпотентность в приложении.
Идемпотентность реализована на основе X-Request-Id, который генерирует клиент при создании заказа. Если в N времени 
клиент отправит запрос, то заказ создан не будет, а будет показан информация по текущему созданному заказа, который
сопоставлен с идентификатором идемпотености.
Идемпотеность реализована при обновлении заказа. Если два и более потока обновляют заказ, только один сможет корректно
его обновить - первый. У остальных версия заказа изменится и транзация завершится с исключением/ошибкой.

ЗАПУСК ПРИЛОЖЕНИЯ
Используется namespace: auth-kuber-service-space

Прежде чем выполнять установку, нужно сопоставить DNS и IP миникуба (это IP адрес ноды кубернетеса, устанавливается на 1
ноду)
Стартуем миникуб: minikube start
Узнать IP кубера (будет IP по котором достучимся до nginx по 80 порту): kubectl get nodes -o wide
Изменить hosts: sudo nano /etc/hosts меняем на {IP_minikube} arch.homework
Для проверки работоспособности выполняем curl:
curl -H "Host: arch.homework" http://{IP_minikube}/auth/health

КОМПИЛЯЦИЯ И ПУШИНГ ОБРАЗА В РЕПОЗИТОРИЙ:
По умолчанию этого делать не нужно, т.к. образы берутся из github.
Если нужно скомпилировать и запушить в docker registry:

1. registry прописать в главном pom и пароль установить в .m2/settings.xml
2. Компилируем микросервисы: mvn clean install
3. Пушим в докер registry: mvn jib:build

СТАРТ СЕРВИСОВ (образы подтянутся из github-репозитория):

1. Стартуем миникуб, если незапущен: minikube start
2. Создаем namespace: kubectl create namespace auth-kuber-service-space
3. Создаем/обновляем кофигурацию и секреты в кубернетес:
   helm upgrade --install common-config charts/common-config/ -f charts/common-config/test.yaml
   helm upgrade --install common-secret charts/common-secret/ -f charts/common-secret/test.yaml
4. Билдим зависимости в чартах:
   helm dependency build ./charts/auth && \
   helm dependency build ./charts/billing && \
   helm dependency build ./charts/notification && \
   helm dependency build ./charts/order && \
   helm dependency build ./charts/delivery && \
   helm dependency build ./charts/inventory && \
   helm dependency build ./charts/auth-validate
4. Скачиваем и поднимаем ingress-controller:
   make -f install-nginx-ingress.mk install

Если поднять не получилось, то скорее всего проблема со скачиванием образа.
Поэтому нужно его скачать, запушить в docker миникуба и снова установить:
docker pull registry.k8s.io/ingress-nginx/controller:v1.15.1@sha256:594ceea76b01c592858f803f9ff4d2cb40542cae2060410b2c95f75907d659e1
minikube image load registry.k8s.io/ingress-nginx/controller:v1.15.1
make -f install-nginx-ingress.mk install

5. С помощью команды kubectl get pods -n auth-kuber-service-space удостоверяемся, что ingress стал READY.
6. Создаем/поднимаем все сервисы:
   helm upgrade --install auth-service ./charts/auth -n auth-kuber-service-space && \
   helm upgrade --install billing-service ./charts/billing -n auth-kuber-service-space && \
   helm upgrade --install notification-service ./charts/notification -n auth-kuber-service-space && \
   helm upgrade --install order-service ./charts/order -n auth-kuber-service-space && \
   helm upgrade --install delivery-service ./charts/delivery -n auth-kuber-service-space && \
   helm upgrade --install inventory-service ./charts/inventory -n auth-kuber-service-space && \
   helm upgrade --install rabbitmq-service ./charts/rabbit -f charts/rabbit/values.yaml -n auth-kuber-service-space && \
   helm upgrade --install auth-validate-service ./charts/auth-validate -f charts/auth-validate/values.yaml -n auth-kuber-service-space
7. Выполняем kubectl get pods -n auth-kuber-service-space и убеждаемся, что все сервисы в статусе RUNNING

ТЕСТИРОВАНИЕ:
Проверка через newman (заранее нужно установить):
newman run postman/SAGA.postman_collection.json -e postman/OtusEnvironment.postman_environment.json
Данный тест проверяет следующее:
1. Создание заказа и успешное его завершение.
2. Создание заказа и нехватку курьеров на его выполнение и откат, показыватся соответствующий ивент.
3. Создание заказа и нехватку товаров на складе и откат, показыватся соответствующий ивент.

newman run postman/SAGA-Idempotance.postman_collection.json -e postman/OtusEnvironment.postman_environment.json
Данный тест проверяет следующее:
1. Создание заказа, проверка его статуса в IN_PROCESS. Затем повторное создание заказа с таким же ключом
   идемпотентности. Убеждаемся, что заказ повторно не создается, а по ключу находится текущий заказ и его статус
   COMPLETED.
   (Но, если вручную через коллекцию заново создавать через 5 секунд, то уже будет создаваться новый заказ.
поэтому с данным ключом идемпотентности избавляемся от случаев случайного двойного нажатия на "создать заказ".)
2. Обновление заказа. Видно, что заказ обновляется корректно. Postman не позволяет выполнять запросы параллельно, 
поэтому лучше вручную добавить коллекцию и выполнить сначала updateOrder, а затем тут же updateOrder1.
Но перед этим выполнить регистрацию, аутентификацию, создать курьера, ивенторри и заказ и только затем обновлять.
В коде установлено засыпание на 5 секунд для тестов. Поэтому в течении 5 секунд оба запроса будут в ожидании ответа от
сервера. По истечении времени один запрос выполнится и возвратится ответ 200, а другой возвратит ответ 304.
