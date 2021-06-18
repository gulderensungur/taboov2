 # Taboo Game

### Prerequisites


* java
  ```sh
  apt-get install default-jdk
  ```

### Installation


Compile all java files in game folder.
   ```sh
   javac *.java
   ```


## Usage

1. Start game server. The words will be loaded at the same time.
   ```sh
   java GameServer
   ```
2. Start clients
   ```sh
   java Client
   ```
3. After all the clients are connected, type "--START" to start the game. That will initialize the teams and start the game.
   ```sh
   --START
   Game is starting...
    _____     _               _____ _                
   |_   _|_ _| |__   ___   __|_   _(_)_ __ ___   ___ 
     | |/ _` | '_ \ / _ \ / _ \| | | | '_ ` _ \ / _ \
     | | (_| | |_) | (_) | (_) | | | | | | | | |  __/
     |_|\__,_|_.__/ \___/ \___/|_| |_|_| |_| |_|\___|
   
   Team A: [Client1, Client3]
   Team B: [Client4, Client2]
   ```
4. Type "--NEXT" to load next word and forbidden words. That will send a new word to a client randomly on Team A and to all clients in Team B.
   ```sh
   --NEXT
   Explain | Word: Swimming | Forbiden words: [water, pool, lake, ocean, suit]
   ```
5. If someone founds the word and type it in the chat, server will detect it.
   ```sh
   Swimming
   Found!!!! You can move to next word by typing "--NEXT"
   ```


## Contact

Mehmet Fatih Okuyan - mehmetfatihokuyan@gmail.com

Gülderen Sungur - gulderensungur96@gmail.com

 
