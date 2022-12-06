import * as protobuf from "./protobuf.js";

const serviceBaseUrl = "/api/v1/game";
const eventsBaseUrl = 'events/v1/game';

function getProtobuf() {
  return window.protobuf ? window.protobuf : protobuf;
}

class BoardView {
  rowCount;
  columnCount;
  #markedLines;
  #filledBoxes;

  constructor(gameState) {
    const board = gameState.board;
    this.rowCount = board.rowCount;
    this.columnCount = board.columnCount;
    this.#markedLines = [
      BitSet(board.playerOneLines),
      BitSet(board.playerTwoLines)]
    this.#filledBoxes = [
      BitSet(board.playerOneBoxes),
      BitSet(board.playerTwoBoxes)
    ];
  }

  lineMarked(row, column) {
    const lineIndex = row * (this.columnCount + 1) + column;

    if (this.#markedLines[0].get(lineIndex)) {
      return 1;
    } else if (this.#markedLines[1].get(lineIndex)) {
      return 2;
    } else {
      return 0;
    }
  }

  boxFilled(row, column) {
    const boxIndex = row * this.columnCount + column;

    if (this.#filledBoxes[0].get(boxIndex)) {
      return 1;
    } else if (this.#filledBoxes[1].get(boxIndex)) {
      return 2;
    } else {
      return 0;
    }
  }
}

class GameClient {
  #gameId;
  #playerId;
  #protoRoot;
  #service;
  #turnResponseType;
  #webSocket;

  constructor(protoRoot) {
    this.#protoRoot = protoRoot;

    const serviceFactory = protoRoot.lookup("dots_boxes_game.GameService");
    this.#service = serviceFactory.create(this.#fetchRpc, false, false);
    this.#turnResponseType = protoRoot.lookupType("dots_boxes_game.TurnResponse");
  }

  getPlayer() {
    return this.#playerId;
  }

  async getGame(gameId, playerId) {
    const result = await this.#service.get({
      uuid: gameId,
      playerId: playerId});

    this.#playerId = playerId;
    this.#gameId = gameId;
    this.#connectGameEvents(this.#gameId);
    return this.#gameFromGameState(result.game);
  }

  async markLine(row, column) {
    console.log(`markLine gameId=${this.#gameId}, playerId=${this.#playerId}`);
    const result = await this.#service.markLine({
      uuid: this.#gameId,
      playerId: this.#playerId,
      row: row,
      column: column});
    console.log(result);

    return result;
  }

  #gameFromGameState(gameState) {
    return {
      currentPlayer: gameState.currentPlayer,
      board: new BoardView(gameState)
    };
  }

  #connectGameEvents(gameId) {
    const eventsUrl = `ws://${window.location.host}/${eventsBaseUrl}/${gameId}`;

    if (this.#webSocket) {
      this.#webSocket.close();
      this.#webSocket.onmessage = null;
      this.#webSocket.onerror = null;
      this.#webSocket.onopen = null;
      this.#webSocket.onclose = null;
      this.#webSocket = null;
    }
    this.#webSocket = new WebSocket(eventsUrl);
    this.#webSocket.binaryType = "arraybuffer";
    this.#webSocket.onmessage = this.#onMessage.bind(this);

    const logEvents = message => event => {
      console.log(`ws: ${message}`);
      console.log(event);
    };

    this.#webSocket.onerror = logEvents("received error");
    this.#webSocket.onopen = logEvents("opened");
    this.#webSocket.onclose = logEvents("closed");
  }

  #onMessage(eventMessage) {
    console.log("ws: received message");
    const buffer = new Uint8Array(eventMessage.data);
    const turnResponse = this.#turnResponseType.decode(buffer);

    console.log(turnResponse);
  }

  async createGame(rowCount, columnCount, playerOneId, playerTwoId, currentIsPlayerOne) {
    const result = await this.#service.create({
      rowCount: rowCount,
      columnCount: columnCount,
      playerOneId: playerOneId,
      playerTwoId: playerTwoId});

    this.#playerId = currentIsPlayerOne ? playerOneId : playerTwoId;
    this.#gameId = result.uuid;
    this.#connectGameEvents(this.#gameId);
    return this.#gameFromGameState(result.game);
  }

  #fetchRpc(method, requestData, callback) {
    console.log(method)

    const serviceUrl = `${serviceBaseUrl}/${method.name.toLowerCase()}`;
    fetch(serviceUrl, {
      method: "POST",
      mode: "same-origin",
      cache: "no-cache",
      headers: {
        "Accept": "application/x-protobuf;charset=UTF-8",
        "Content-Type": "application/x-protobuf;charset=UTF-8"
      },
      body: requestData
    })
    .then(response => {
      if (!response.ok) {
        throw new Error(`Game client network request failed with HTTP status ${response.status}.`);
      }
      return response.arrayBuffer();
    })
    .then(arrayBufferResponse => {
      callback(null, new Uint8Array(arrayBufferResponse))
    })
    .catch(error => callback(error, null));
  }
}

let gameClient = null;

export async function getGameClient() {
  if (gameClient) {
    return gameClient;
  } else {
    gameClient = new GameClient(await loadProtoFiles());
    return gameClient;
  }
}

async function loadProtoFiles() {
  const protobuf = getProtobuf();
  const root = await protobuf.load("../dotsboxes_service.proto");
  return root.resolveAll();
}