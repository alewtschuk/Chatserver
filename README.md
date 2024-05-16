# Project Number/Title {#mainpage}

* Author: Kai Sorensen and Alex Lewtschuk
* Class: CS455 [Distributed Systems] Section #001

## Overview

This project consists of two main programs: ChatClient and ChatServer. ChatClients can connect
to the ChatServer, join a chat room, and start chatting. It implements basic IRC protocol.

## Manifest

ChatServer: implements a multithreaded server
ChatClient: implements a multithreaded client
CmdMsg: the only object that is sent back and forth between the clients and server

## Building the project

A makefile is included in the team-6 main directory. It will compile the necessary files, preparing them for execution.
Execute the makefile from the main team-6 directory as follows:
```
make all
```
To clean and reset the project from the team-6 directory, use:
```
make clean
```

## Features and usage

We decided to split the server and client into two separate Java files. That means they have to be ran separately.
Start by launching the server. Here is its usage statement. Use port number 5128.
```
$ java ChatServer -p 5128 -d <debug-level(0|1)>
```
Then, you can run some clients. Up to 100 clients can connect to the server at a time.
```
$ java ChatClient
```

## Testing

For this testing we essentially did end user testing. We thought of a list of what the user could do when running the server and then proceeded to test running different combinations of commands with different number of users on the server. This allowed us to catch most of the edge cases that we could think of. We ran commands when the client was connected to the server and when it was disconnected. The edge case generation that we did took care of all the issues that we ran into and the checks that we have implemented into our code took care of them all.

### Known Bugs

There are certainly some edge cases that we haven't yet discovered. However, if the user uses IRC
protocol properly, there shouldn't be any errors.

## Reflection

Kai:
This project was quite fun to design and implement, but it was hell to debug. There were so many edge cases, and eventually it became so complex that it was difficult to simulate scenarios other than running the whole project and going through the scenario manually. If that's how complex 1000 lines of code can be, I can't imagine what debugging a 10000 line server would be like. Obviously we'd be testing it using methods that we haven't yet been exposed to. At one point I realized that the client could be thought of as a state machine... too late.

A super annoying problem that we had was the ctrl-C server termination. It was my first time dealing with 
Runtime.getRuntime().addShutdownHook(). It makes sense, but we errors that were difficult to trace. The main server
thread was still stuck on Socket.accept(), and when the Socket Closed exception was thrown, it would just go back into
the infinite loop and throw it again. That took a while to understand. Furthermore, ALex thought I had already handled that error and spent time searching his code for a bug that didn't exist.

Alex and I didn't do any pair progamming for this project. I implemented the server, he implemented the client, and we met in the middle. I'm not sure if we could have developed it any more efficiently. For persepctive, I have very little idea how ChatClient works, but understand every one of ChatServer's 420 lines. It worked for us, but it might have been more productive to use pair programming from the start. In that case we would have had to make many more concessions to each other about how to write the code, and maybe wouldn't have written as quickly. Though, I bet we eventually wouldn't have had as much confusion with the bugs.




Alex:
I absolutley loved thsi project! It was a lot of fun to build and work on even though it was a pain to debug when we ran into issues. Like Kai said there was a lot of edge cases, but I think we delt with most of them. It was a bit dificult to detemine what cases there were unless we ran the program and made a list of everything a user could do, basically end user testing, in order to see what the best options were for what errors we needed to handle. The team component was nice, Kai was great to work with and we were able to throw around ideas easily. 

Something that we ran into that caused some issues was when the client and server were terminated. There was an issue getting the clients to propperly disconnect and ensure that they were reconfigured to their base state so they could reconnect again if the server came back online. Came to find out I had a bad practice in my Listener and realized that the ObjectInputStream objects were both called in so it was updateing in locally in Listener and not the in value for the client program. It was a quick fix, after I spent 45 minutes banging my head against my keyboard :). 

We definitly didn't pair program as it was defined in class and I am not as aquainted with the ChatServer as I would like to be, but that being said we did a good job at commmunicating our ideas and making sure we know how the code works. I feel like I have a decent idea of how ChatServer works overall. The main reason we both worked on the individual files more is to avoid Github merge conflicts.


## Sources used

We mainly referenced our PingPong projects and the ObjectServer example.
ChatGPT was used to clarify concepts, but never to generate code.