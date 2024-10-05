### Запуск автотестов:

## Необходимые приложения:
1. Среда и инструменты для разработки последних версий: JRE, JDK
2. IntelliJ IDEA Community Edition - Интегрированная среда разработки
3. Docker Desktop - Платформа контейнеризации
4. Браузер - Google Chrome

## Инструкция по запуску автотестов:

1. Проверить, что порты 8080, 9999, 5432 или 3306 (в зависимости от выбранной СУБД) свободны
2. Склонировать репозиторий (https://github.com/sergei0111/qa_diplom)
3. Открыть его в Intelij IDEA
4. Ввести в терминале команду docker-compose up
5. Для запуска с поддержкой
* СУБД MySQL:
Ввести в терминале команду java -jar ./artifacts/aqa-shop.jar
Нажать ctrl дважды - открыть окно Run anything. Ввести команду gradlew clean test
* СУБД PostgreSQL:
Ввести в терминале команду  java "-Dspring.datasource.url=jdbc:postgresql://localhost:5432/app" -jar artifacts/aqa-shop.jar
Нажать ctrl дважды - открыть окно Run anything. Ввести команду gradlew clean test "-Ddb.url=jdbc:postgresql://localhost:5432/app"
6. Для генерации Allure отчета нажать: .\gradlew allureServe