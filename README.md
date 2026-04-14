# MyTinyParser

Лёгковесная Java-библиотека для ручного парсинга `multipart/form-data` запросов. Не использует встроенные механизмы сервлет-контейнера или фреймворка — весь разбор реализован с нуля поверх сырого `InputStream`.

## Назначение

Предназначена для подключения в виде JAR-зависимости к любому Jakarta Servlet-based приложению (например, Spring Boot). Используется там, где нужен полный контроль над процессом разбора multipart-запроса — например, при загрузке файлов с вложенными поддиректориями в имени файла.

Типичный сценарий использования:

```
POST /resource?path=/storage_folder
Content-Type: multipart/form-data; boundary=----boundary

// тело запроса содержит один или несколько файлов
// если имя файла содержит путь (upload_folder/test.txt),
// это сохраняется в ContentDisposition для дальнейшей обработки
```

## Принцип работы

Парсер работает как конечный автомат с четырьмя состояниями:

```
LOOKING_BOUNDARY → FOUND_BOUNDARY → FOUND_DISPOSITION → LOOKING_BOUNDARY (следующая часть)
                                                       → REACHED_END (конец стрима)
```

Для каждой части запроса:

1. `ParserService` извлекает boundary из заголовка `Content-Type`
2. `extractContentDisposition()` читает стрим побайтово, ищет `--boundary`, затем `\r\n\r\n` — конец заголовков. Возвращает `ContentDisposition` с `fileName`, `name`, `type`
3. `extractResourceContent()` возвращает `BoundedInputStream` — читает байты файла до маркера `\r\n--boundary`
4. `MyTinyParserApplication.parseAll()` в цикле собирает все части в `List<Parts>`

## Подключение как библиотека

Собрать JAR:
```bash
./mvnw package
```

Установить в локальный Maven-репозиторий:
```bash
./mvnw install
```

Добавить зависимость в `pom.xml` основного проекта:
```xml
<dependency>
    <groupId>org.me</groupId>
    <artifactId>MyTinyParser</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Требования

- Java 21+
- Jakarta Servlet API 6.1
