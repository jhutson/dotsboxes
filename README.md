# Dots and Boxes Game

[![Java CI](https://github.com/jhutson/dotsboxes/actions/workflows/ci.yml/badge.svg?event=push)](https://github.com/jhutson/dotsboxes/actions/workflows/ci.yml)

This project contains a set of services that make up an online version of the [Dots and Boxes game](https://en.wikipedia.org/wiki/Dots_and_Boxes). 

## Getting Started

### Docker Containers
In the root directory, run ```docker compose up -d``` to start the docker containers for nginx, DynamoDB, and LocalStack. These are used by the backend.

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

Run this command to create the local DynamoDB table that stores game state:

```./gradlew game-service:createLocalTables```

AWS configuration needs to be modified to point at LocalStack. One way to do this is to modify the ```config``` and ```credentials``` files under ```~/.aws```.

For ```~/.aws/config```, ensure the default profile is using the right region:

```
[default]
region=us-west-2
```

For ```~/.aws/credentials```, use test values:

```
[default]
aws_access_key_id="test"
aws_secret_access_key="test"
```

Once the AWS configuration is in place, run this command to set up the SNS game events topic in LocalStack:

```
aws --endpoint-url=http://localhost:4566 sns create-topic --name game-turn-events
```

With the steps completed for docker, backend, and frontend, open a browser and navigate to [http://localhost](http://localhost). You should see the URL changed to http://localhost?gameUUID, where gameUUID is an identifier for a new game. This browser window is for player one.

To play as player two, copy the URL and paste it in a new browser, appending `#p2` to the URL. 

You should now be able to play as player one and two across the two browser windows. You should also see moves performed by one player reflected in the other player's window.
