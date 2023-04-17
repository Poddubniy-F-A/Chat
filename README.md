# Chat
Для запуска - сначала ServerLauncher, затем ClientLauncher в нескольких инстансах, если необходимо обеспечить "переписку" между несколькими пользователями.
Число пользователей "онлайн" ограничено размером пула потоков executorService в классе Server.
Для авторизации/создания "аккаунтов" используется БД, сохраняющая поступившие в ходе сессии данные пользователей для последующих запусков.
Чат поддерживает переписку только между "онлайн" пользователями (если "человек" не в сети, написать ему нельзя), но зато имеет функцию "рассылки" (через общий чат, текст в котором виден всем в "онлайне").
Данные переписки сохраняются для последующих запусков в уникальных для каждого диалога файлах (при запуске подгружаются последние HISTORY_SIZE в классе ChatController строчек переписки).
Ошибки и моменты "подключения"/"отключения" пользователей сети фиксируются в лог-файле.
