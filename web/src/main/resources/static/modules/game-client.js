import * as protobuf from "./protobuf.js";

const serviceBaseUrl = "/api/v1/game"

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
  #protoRoot;
  #service;
  #gameId;

  constructor(protoRoot) {
    this.#protoRoot = protoRoot;

    const serviceFactory = protoRoot.lookup("dots_boxes_game.GameService");
    this.#service = serviceFactory.create(fetchRpc, false, false);
  }

  #gameFromGameState(gameState) {
    return {
      currentPlayer: gameState.currentPlayer + 1,
      board: new BoardView(gameState)
    };
  }

  async createGame(rowCount, columnCount) {
    const result = await this.#service.create({
      rowCount: rowCount,
      columnCount: columnCount,
      playerOneId: "p1",
      playerTwoId: "p2"});

    this.#gameId = result.uuid;
    return this.#gameFromGameState(result.game);
  }

  async getGame(gameId) {
    const result = await this.#service.get({
      uuid: gameId,
      playerId: "p1"});

    this.#gameId = gameId;
    return this.#gameFromGameState(result.game);
  }

  async markLine(row, column) {
    console.log(`gameId=${this.#gameId}`);
    const result = await this.#service.markLine({
      uuid: this.#gameId,
      playerId: "todo",
      row: row,
      column: column});

    return result;
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

function fetchRpc(method, requestData, callback) {
  console.log(method)
  console.log(requestData)

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

async function loadProtoFiles() {
  const protobuf = getProtobuf();
  const root = await protobuf.load("../dotsboxes_service.proto");
  return root.resolveAll();
}