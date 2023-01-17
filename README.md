# Dots and Boxes Game

[![Java CI](https://github.com/jhutson/dotsboxes/actions/workflows/ci.yml/badge.svg?event=push)](https://github.com/jhutson/dotsboxes/actions/workflows/ci.yml)

This project contains a set of services that make up an online version of the [Dots and Boxes game](https://en.wikipedia.org/wiki/Dots_and_Boxes). 

## Getting Started

### Docker Containers
In the root directory, run ```docker compose up -d``` to start the docker containers for nginx and DynamoDB. These are used by the backend.

### Backend
The backend projects require Java 17. Recommend to use [SDKMAN!](https://sdkman.io) to manage JDK installs. To install Java 11 with SDKMAN!, run:

```sdk install java 17.0.5-amzn```

 Once you've cloned the project from git, run the following to compile, test, and package the project:

```./gradlew build```

### Frontend
The frontend project requires node.js. Recommend installing Node Version Manager (nvm) to manage node versions. With mvn install, run the following commands:

```
cd frontend/game
nvm use
```

If the output of nvm includes "Now using node" and a version number, you're good to go. Otherwise, you'll see a command quoted after the message "You need to run". Run the `nvm install` command shown to install the specific version needed. Once the correct version is installed, rerun `nvm use` and you should see the "Now using node" message.

Restore dependencies by running:

```npm install```

With node and dependencies installed, you can start the local vite server by running:

```npm run dev```

### Test local setup
With the steps completed for docker, backend, and frontend, open a browser and navigate to [http://localhost](http://localhost). You should see the URL changed to http://localhost?gameUUID, where gameUUID is an identifer for a new game. This browser window is for player one.

To play as player two, copy the URL and paste it in a new browser, appending `#p2` to the URL. 

You should now be able to play as player one and two across the two browser windows. You should also see moves performed by one player reflected in the other player's window.
