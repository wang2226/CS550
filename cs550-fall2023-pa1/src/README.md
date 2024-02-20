# A Simple Napster Style Peer to Peer File Sharing System
## _CS 550 Programming Assignment #1_



Peer-to-Peer(P2P) Technologies are being widely used for sharing the data between the servers and the clients. One of the major technology for file sharing that is implemented nowadays is the Napster-Style Peer-to-Peer File Sharing System.
The older versions of the systems used to have a single server which stores the files in its directory that are received from the clients. The major drawback of these systems was that if a new file has been created in one of the peers, it must be transferred to the server before another peer can access it, which delays the process of transfer from one peer to another. This can be conquered using the Napster system which allows the peer to peer file transfer.

With this programming assignment, We can learn the following two things:
- First to  familiarize with sockets, processes, threads, makefiles
- Second to learn the design and internals of a Napster-style peer-to-peer (P2P) file sharing system

## Design
The assignment is designed using Java where we have used the concepts of Socket Programming and Multi-threading. For establishing the connections between the Server and the Clients, we have used TCP/IP protocol using the sockets.
- **Central_Indexing_Server Class** implements the Indexing Server startup, listen on the service port and create work thread
- **cisServices Class** implements all the central indexing server functionality, it runs on the thread. This class provide the following interface to the peer clients:
    - registry (String peerId, String fileName) - invoked by a peer to register all its files with the indexing server
    - search(String fileName) - search the index and return all the matching peers to the requestor
- **Peer Class** implements the peer client and peer server startup
- **peerClient Class** implements peer as client functionality, the user specifies a file name with the indexing server using "lookup". This class provide the following interface :
    - runInteractive() - run client functionality interactively 
    - runBatch(String level) - run client functionality in batch, use for evaluation and measurement the behavior of our system
    - obtain(String fileName, String ownerId, String downPath) - invoked by a peer to download a file from another peer
- **peerServer Class** the peer waits for requests from other peers and sends the requested file when receiving a request
- **getParameter Class** used to read parameters for system configurability

1. **Central_Indexing_Server.java** contains Central_Indexing_Server Class and cisServices Class
2. **Peer.java** contains Peer Class,  peerClient Class and peerServer Class
3. **getParameter.java** contain getParameter Class

## Supporting programs and tools

- **Create_DataSet.sh:**  generate test datasets for each peer automatically
- **config.properties:** Configuration files for each node, including Ip, port and shared file directory etc.
- **Makefile:**  script used for automating the build process of the project
- **data_plot.py:** plot performance evaluation data in figures graphically
- **pssh:** coordinate the bootstrapping of our P2P system, and automate and conduct the performance evaluation concurrently across the P2P system.
- **node-hosts:** hosts file from which pssh read hosts names. Each line in the host file are of the form [user@]host[:port] and can include blank lines and comments lines beginning with “#”.

## Deployment
Deploy 2 peers and 1 indexing server over 3 VMs.

On each peer node, run Create_DataSet.sh, using peerid as a script parameter.

```sh
Create_DataSet.sh 1
```
When the script runs successfully, it generates 10k small files (1KB), 1K medium files (1MB) and 8 large files (1GB) in a directory named shared.

Modify the configuration information for each node in the config.properties file. The parameters are described as follows：

| Parameter | Meaning |
| ------ | ------ |
| Peer_Node | this peer node number |
| File_Node | peer node number other than this node |
| Central_Indexing_Server_Ip | IP of Idexing Server |
| Central_Indexing_Server_Port | server port number of Idexing Server |
| Peer1_Ip  | IP of Peer node 1 |
| Peer1_Client_Port | port number of peer 1 as client |
| Peer1_Server_Port | port number of peer 1 as sever |
| Peer1_Shared_Directory | share file directory of peer 1 |
| Peer2_Ip  | IP of Peer node 2 |
| Peer2_Client_Port | port number of peer 2 as client |
| Peer2_Server_Port | port number of peer 2 as sever |
| Peer2_Shared_Directory | share file directory of peer 2 |

## Building for source

For compilation:

```sh
make
```
For clean up the compilation environment:
```sh
make clean
```

## Run
Startup Indexing Server:

```sh
java Central_Indexing_Server
```
Run peer interactively：
```sh
java Peer
```
Do a weak scaling scalability study：
```sh
pssh -ih node-hosts -A java Peer weak 
```
Do a strong scaling scalability study：
```sh
pssh -ih node-hosts -A java Peer strong 
```
