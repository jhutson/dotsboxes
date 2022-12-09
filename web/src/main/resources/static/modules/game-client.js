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

  getBoxIndex(row, column) {
    return row * this.columnCount + column;
  }

  boxFilled(row, column) {
    const boxIndex = this.getBoxIndex(row, column);

    if (this.#filledBoxes[0].get(boxIndex)) {
      return 1;
    } else if (this.#filledBoxes[1].get(boxIndex)) {
      return 2;
    } else {
      return 0;
    }
  }
}

const doNothingWithArgument = _ => {};

class GameClient {
  #gameId;
  #playerId;
  #sequenceNumber;
  #protoRoot;
  #service;
  #turnResponseType;
  #webSocket;

  #onTurnCompleted;

  constructor(protoRoot) {
    this.#protoRoot = protoRoot;

    const serviceFactory = protoRoot.lookup("dots_boxes_game.GameService");
    this.#service = serviceFactory.create(this.#fetchRpc, false, false);
    this.#turnResponseType = protoRoot.lookupType("dots_boxes_game.TurnResponse");
    this.setOnTurnCompleted(null);
  }

  getPlayerId() {
    return this.#playerId;
  }

  setOnTurnCompleted(callback) {
    if (callback === null) {
      this.#onTurnCompleted = doNothingWithArgument;
    } else {
      this.#onTurnCompleted = callback;
    }
  }

  async getGame(gameId, playerId) {
    const result = await this.#service.get({
      uuid: gameId,
      playerId: playerId});

    this.#gameId = gameId;
    this.#playerId = playerId;
    this.#sequenceNumber = result.game.sequenceNumber;

    if (result.game.outcome === null) {
      this.#connectGameEvents(this.#gameId);
    }
    return this.#gameFromGameResponse(result);
  }

  async createGame(rowCount, columnCount, playerOneId, playerTwoId, currentPlayerId) {
    const result = await this.#service.create({
      rowCount: rowCount,
      columnCount: columnCount,
      playerOneId: playerOneId,
      playerTwoId: playerTwoId});

    this.#gameId = result.uuid;
    this.#playerId = currentPlayerId;
    this.#sequenceNumber = result.game.sequenceNumber;
    this.#connectGameEvents(this.#gameId);

    return this.#gameFromGameResponse(result);
  }

  async markLine(row, column) {
    console.log(`markLine gameId=${this.#gameId}, playerId=${this.#playerId}, sequenceNumber=${this.#sequenceNumber}`);
    const result = await this.#service.markLine({
      uuid: this.#gameId,
      sequenceNumber: this.#sequenceNumber,
      playerId: this.#playerId,
      row: row,
      column: column});

    return result;
  }

  #gameFromGameResponse(gameResponse) {
    return {
      currentPlayerIndex: gameResponse.game.currentPlayer,
      playerIndex: gameResponse.thisPlayer,
      board: new BoardView(gameResponse.game),
      outcome: gameResponse.game.outcome
    };
  }

  #disconnectGameEvents() {
    if (this.#webSocket) {
      console.log("Disconnecting game events WebSocket.")
      this.#webSocket.close();
      this.#webSocket.onmessage = null;
      this.#webSocket.onerror = null;
      this.#webSocket.onopen = null;
      this.#webSocket.onclose = null;
      this.#webSocket = null;
    }
  }

  #connectGameEvents(gameId) {
    const eventsUrl = `ws://${window.location.host}/${eventsBaseUrl}/${gameId}`;

    this.#disconnectGameEvents();
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

    this.#sequenceNumber = turnResponse.sequenceNumber;

    console.log(turnResponse);

    if (turnResponse.outcome) {
      this.#disconnectGameEvents();
    }
    this.#onTurnCompleted(turnResponse);
  }

  #fetchRpc(method, requestData, callback) {
    console.log(`Invoking service method ${method.name}.`);

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