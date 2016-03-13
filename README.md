# Setup Guide

## Compile and run using the install script

1. __Download__ and Extract the source files or __clone__ repository from [https://github.com/yhpoh91/SyncSync.git](https://github.com/yhpoh91/SyncSync.git)
2. Compile:

 i. For Windows: Double click to execute _install.bat_.
 
 ii. For Mac: Execute _install.sh_ using __Terminal__. _Note: You have to be in the same directory._

3. Navigate to the extracted directory using __Terminal__ (Mac) or __Command Prompt__ (Windows).
4. Run: ```java main.Main <port>``` _Note: replace <port> with port number desired._

## Compile and run the program manually

1. __Download__ and Extract the source files or __clone__ repository from [https://github.com/yhpoh91/SyncSync.git](https://github.com/yhpoh91/SyncSync.git)
2. Navigate to the extracted directory using __Terminal__ (Mac) or __Command Prompt__ (Windows).
3. Compile:

 i. For Windows: ```javac -sourcepath .;main:utils main/Main.java```
 
 ii. For Mac: ```javac -sourcepath .:main:utils main/Main.java```
 
4. Run: ```java main.Main <port>``` _Note: replace <port> with port number desired._

# User Guide

To change the __Shared Folder Path__:

1. Input the command ```path``` into the application. Not prompt or output will be given.
2. Input the desired path (relative to the current path, similar to the path used in the __Terminal__/__Command Prompt__'s change directory ```cd``` command) in a new line. It has to be the immediate new line after the command.
3. Verify that the new path is assigned to the application.

To start synchronizating with peer:

1. Input the command ```connect``` into the application. No prompt or output will be given.
2. Input the IP of peer application in a new line. It has to be the immediate new line after the command. No prompt or output will be given. For example: ```192.168.1.2```
3. Input the Port number of the peer application in a new line. It has to be the immediate new line after the IP address. No prompt or output will be given. For example: ```5700```
4. Verify that the application is connecting to the specified IP address and Port number.
5. Synchronization will start. Status update will be displayed during the process of synchronization.

To exit from the application:

1. Input the command ```exit``` into the application. No prompt or output will be given.

A sample screenshot of the application is provided below as part of the user guide.

![alt text](https://github.com/yhpoh91/SyncSync/raw/master/screenshot.png "Sample Screenshot")
