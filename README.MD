# AggreGate Count Plugin
###### *версия сервера на котором производился тестовый запуск: `v6.34.01-1894`

Данный плагин был разработан для первоначального знакомства с aggregate sdk.<br>
Плагин запускает несколько потоков: один для отслеживания изменений переменных настроек, 
другой для непосредственного изменения счётчика с указанным в настройках интервалом.
Запись на диск осуществляется из основного потока. Плагин имеет несколько вкладок с настройками:
 - Настройки счётчика.
 - Настройки хранения файлов.

![Основной вид плагина](/readme-files/plugin.png)
При старте сервера все обращения к методам записываются в файл `C:\test_out.txt`. После чтения
настроек из контекста сервера запись продолжается в файл указанный в настройках. Если не удаётся
создать файл для вывода информации по причине некорректного указания пути или невозможности
изменения файла, то запись будет производится в дефолтное расположение `C:\test_out.txt`. Изменения настроек плагина,
не требуют перезапуска сервера, настройки применяются на лету. Интервал обновления настроек
константен и равен `10 сек.` В настройках счётчика можно указать\изменить префикс добавляемый к каждой новой
записи, и время через которое будет создаваться новая запись.

## Сборка плагина
Поскольку вам придётся столкнуться с главным врагом здравого смысла, graddle'ом - всё что угодно
может пойти не так. Если сборка окончилась не удачей, то готовый собранный jar файл находится
в каталоге `readme-files`. Загрузить его можно по [ссылке](/readme-files/sazonov-counter-plugin.jar).

Сперва необходимо скачать сертификаты с сайта AggreGate, для этого откройте в браузере [ссылку](https://store.aggregate.digital/service/rest/repository/browse/maven-public/)<br>
Для Google Chrome нажмите слева от строки адреса нажмите `"View Site Information" -> "Connection is secure" -> "Certificate is valid"` <br>
Перейдите на вкладку `"Details"` и на каждом из представленных сертификатов (3 шт) нажмите `"Export"`. Для уменьшения количества возможных 
проблем при импорте, используйте Тип файла: `"DER-encoded binary"`. Имея все 3 файла на диске, используйте следующие команды
для импорта сертификатов:
- `keytool -import -alias "_aggregate" -keystore "%JAVA_HOME%\lib\security\cacerts" -file {Указать путь к файлу _.aggregate.digital.der}`
- `keytool -import -alias "_Global_agr" -keystore "%JAVA_HOME%\lib\security\cacerts" -file {Указать путь к файлу GlobalSign.der}`
- `keytool -import -alias "Global_r6" -keystore "%JAVA_HOME%\lib\security\cacerts" -file {Указать путь к файлу GlobalSign GCC R6 AlphaSSL CA 2023.der}`

В случае запроса пароля при импорте, попробуйте `changeit`

В инструкции подразумевается что в системе на которой производятся действия существует диск `D:\ `, если он отсутствует,
то терминал следует открыть от имени администратора, и использовать диск `C:\ `

Выполнить последовательно команды в терминале:

- `mkdir D:\AggreGate-count-plugin`
- `D:`
- `cd D:\AggreGate-count-plugin`
- `git clone https://github.com/Sznv91/AggregeteCountPlugin.git`
- `cd .\AggregeteCountPlugin`
- `.\gradlew jar`
- `cd .\sazonov-counter-plugin`
- `cp .\sazonov-counter-plugin.jar {ArrgerateServerInstallPath}\plugins\context`

Готово, можете перезапустить сервер. При запуске сервера будет создан файл `C:\test_out.txt` в котором можно
наблюдать последовательность обращения к методам плагина при включении сервера.

Спасибо за внимание.