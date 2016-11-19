Описание:
- программа для обработки логов
- сканирует только указанную папку, далее по дереву не идет
- (папка по умолчанию 'src/logs/')
- несколько готовых логов лежит в папке по умолчанию
- работает с логами расширением .log и кодировкой UTF-8 without BOM

- пользователь может задать:
- несколько фильтров для записей;
- тип группировки;
- количество thread для обработки .log файлов;
- путь к результирующему файлу и его имя;
- (путь по умолчанию src/logs/LogToolResult/LogToolOutput.log)





Simple tool for logs analysis

There is a directory that contains multiple log files. The number of files can be big and the number of lines in one log can be also big.

Each log record contains at least time, username and custom message. Exact format is up to you.
Input parameters:
Filter parameters - at least one should be specified -
·         Username,
·         Time period,
·         Pattern for custom message.
Grouping parameters - at least one should be specified -
·         Username,
·         Time unit (e.g. 1 hour, 1 day, 1 month).
Other parameters -
·         Count of threads used to process files. Each file can be processed in separate thread. (default=1)
·         Path or filename to output file.

Tool should scan directory for log files, read log files and filter log records that conform to user input and produce output given below.
Output

·         Single file with all filtered log records.
·         Print aggregate statistics -
o    Count of records grouped by grouping input parameters.