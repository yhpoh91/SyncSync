# Setup

## Compile and run using the install script

1. __Download__ and Extract the source files or __clone__ repository from [https://github.com/yhpoh91/SyncSync.git](https://github.com/yhpoh91/SyncSync.git)
2. Compile:
 i. For Windows: Double click to execute _install.bat_.
 ii. For Mac: Execute _install.sh_ using __Terminal__. _Note: You have to be in the same directory.
3. Navigate to the extracted directory using __Terminal__ (Mac) or __Command Prompt__ (Windows).
4. Run: ```java main.Main <port>``` _Note: replace <port> with port number desired._

## Compile and run the program manually

1. __Download__ and Extract the source files or __clone__ repository from [https://github.com/yhpoh91/SyncSync.git](https://github.com/yhpoh91/SyncSync.git)
2. Navigate to the extracted directory using __Terminal__ (Mac) or __Command Prompt__ (Windows).
3. Compile:
 i. For Windows: ```javac -sourcepath .;main:utils main/Main.java```
 ii. For Mac: ```javac -sourcepath .:main:utils main/Main.java```
4. Run: ```java main.Main <port>``` _Note: replace <port> with port number desired._
