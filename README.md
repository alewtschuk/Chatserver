# Chat Server

* Author: Kai Sorensen and Alex Lewtschuk
* Class: CS455 [Distributed Systems]

## Overview

This project consists of two main programs: ChatClient and ChatServer. ChatClients can connect
to the ChatServer, join a chat room, and start chatting. It implements basic IRC protocol.

## Manifest

ChatServer: implements a multithreaded server
ChatClient: implements a multithreaded client
CmdMsg: the only object that is sent back and forth between the clients and server

## Building the project

A makefile is included in the directory. It will compile the necessary files, preparing them for execution.
Execute the makefile from the main team-6 directory as follows:
```
make all
```
To clean and reset the project from the team-6 directory, use:
```
make clean
```

## Features and usage

The server and client have been split into two separate Java files. That means they have to be ran separately.
Start by launching the server. Here is its usage statement. Use port number 5128.
```
$ java ChatServer -p 5128 -d <debug-level(0|1)>
```
Then, you can run some clients. Up to 100 clients can connect to the server at a time.
```
$ java ChatClient
```
