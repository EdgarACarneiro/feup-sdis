# PRIMEIRO PROJECTO SDIS - 'Distributed Backup Service'

Serviço de Backup distribuido num LAN com ambiente cooperativo, usando o espaço livre do disco. 

## [GUIÃO](https://web.fe.up.pt/~pfs/aulas/sd2018/projs/proj1/proj1.html)

## Build Tips (Terminal)
```
# Linux
$ find -name "*.java" > sources.txt
$ javac @sources.txt

:: Windows
> dir /s /B *.java > sources.txt
> javac @sources.txt
```

## Running

In order to start the `rmiregistry`, use the following command:
```
$ rmiregistry &
```

To start a peer, use the following command as an example:
```
$ java Main/Peer 2.0 2 teste2 224.0.3:8888 224.0.0.4:8888 224.0.0.5:8888
```

To start the TestApp, use the following command as an example:
```
$ java Main/TestApp localhost/teste2 BACKUP 1.png 1
```
