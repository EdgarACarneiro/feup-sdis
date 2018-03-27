# PRIMEIRO PROJECTO SDIS - 'Distributed Backup Service'

Serviço de Backup distribuido num LAN com ambiente cooperativo, usando o espaço livre do disco.

## [GUIÃO](https://web.fe.up.pt/~pfs/aulas/sd2018/projs/proj1/proj1.html)

## Suposições:
 * Possibilidade de mais do que um servidor por computador
 * Cada servidor guarda ficheiros (ou parte de ficheiros) e é identificado através de um id unico
 * Pode exisir perca ou duplicação de mensagens, mas enquanto o servidor retransmitir a mensagem eventualmente sera recebida
 * Perca de dados de um servidor é independente da perca de dados de outro qualquer servidor. Ficheiros de metadata nunca são perdidos.
 * Os participantes do servio não modificam ou eliminam a informação guardada.

## Serviço
 * Computadores são peers (cada com um ID)
 * Ficheiros backup através de replicação em vários servidores
 * Each file has and identifier -> ver como no guião (usa SHA256)
 * Divisão dos ficheiros em chunks e cada chunnk é 'backed up' individualmente -> chunk de até 64KBytes
 * Um peer não é obrigado guardar todos os chunks de um ficheiro ou até qualquer chunk
 * Cada ficheiro tem um 'desired replication degree (DRD)' que traduz o número de replicações de cada um dos seus chunks pelos peers
 * Funcionalidade de reclamar espaço no disco -> obriga a libertar alguns chunks -> criar novos chunks noutros peers para manter o DRD
 * Funcionalidade eliminar ficheiro -> eliminar todos os chunks desse ficheiro (Note: modificar um ficheiro é apagar e adicionar novo)

## Peer Protocol
 * Existência de uma initator-peer, usada para iniciar sub-protocolos
 * Todos os subprotocolos usam um canal multicast (canal de controlo MC) para trocar mensagens de controlo.
 * Os cananis multicast MDB e MDR são usados respetivamente para backup e para restaurar data chunks.
 * #### Utils.Message Format and Fieldd Encoding (ver guião) -> Informação geral do formato das mensagens
 * #### Chunk backup subprotocol (ver guião) -> Como implmementar o protocolo de backup
**_Importante_**: Um peer nunca guarda chunks dos seus próprios ficheiros
 * #### Chunk restore protocol (ver guião) -> Como implmementar o protocolo de restauro
 * #### File deletion subprotocol (ver guião) -> Como implmementar o protocolo de eliminação de ficheiros
 * #### Space reclaiming subprotocol (ver guião) -> Como implementar o protocolo de reclamação de espaço
**_Importante_**: Se quiser melhorar o protocolo , seguir guias de '3.6 Protocol Enhancement'

## Interface
 * Verificar especificações no guião. Serão implementadas 5 funcionalidaes: backup, restaurp, eliminação, espaço de armazenamento e informação local do serviço

## Aspetos de Implementação
 * Só se pode usar Java SE. Cada peer tem de guardar cada chunk como um ficheiro no sistema.
 * Usar RMI como protocolo de comunicação
 * Ver no guião como escolha de protocolo de transporte afeta a chamada dos arugmentos
 * Um serviço deve descartar mensagens que não compreende
