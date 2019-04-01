# Как это запустить
## = UNIX - системы =
1) Заходим на хелиос, пробрасывая порты (ssh -L [port]:localhost:[port] -p 2222 [login]@helios.cs.ifmo.ru) 
2) Заходим на хелиос без проброса портов (через другой терминал)
3) Заходим в папку с проектом на 3 и 4 терминале терминале
4) Запускаем UDPTunnel.jar в режиме сервера на helios на указанном порте (который пробрасывали) (java18 -jar UDPTunnel.jar server [port])
5) Запускаем UDPTunnel.jar в режиме клиента на своей машине в режиме клиента (java -jar UDPTunnel.jar client [port])
6) Запускаем Lab6_Server.jar на проброшенном порте (в моем случае java18 -jar Lab6_Server.jar [port] [save_file])
7) Запускаем Lab6_Client.jar на проброшенном порте (java -jar Lab6_Client.jar localhost [port])

## = Бесполезный Windows =
Все то же самое, только пробрасывайте порты через Putty.
