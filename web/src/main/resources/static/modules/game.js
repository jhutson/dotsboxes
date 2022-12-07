import { getGameClient } from './game-client.js';

const FILLED_LINE_STYLES = [null, 'lf1', 'lf2'];
const FILLED_BOX_STYLES = [null, 'bf1', 'bf2'];

class BoardUIBuilder {
  boardState;
  playerIndex;
  boardArea;
  board;

  constructor(gameState, boardArea, board) {
    this.boardState = gameState.board;
    this.playerIndex = gameState.playerIndex;
    this.boardArea = boardArea;
    this.board = board;
  }

  build() {
    const rowCount = this.boardState.rowCount;
    const columnCount = this.boardState.columnCount;

    this.boardArea.style.setProperty("--db-column-count", columnCount);
    this.boardArea.style.setProperty("--db-row-count", rowCount);
    this.boardArea.style.setProperty("--db-highlight",
      this.playerIndex == 0 ? "var(--db-highlight-one)" : "var(--db-highlight-two)"
    );

    for (let row = 0; row < rowCount * 2 + 1; ++row) {
      if (row % 2 == 0) {
        this.#populateHorizontalRow(row);
      } else {
        this.#populateVerticalRow(row);
      }
    }
  }

  #populateHorizontalRow(row) {
    for (let column = 0; column < this.boardState.columnCount; column++) {
      this.board.appendChild(this.#createDot());
      this.board.appendChild(this.#createLine("lh", row, column));
    }
    this.board.appendChild(this.#createDot());
  }

  #populateVerticalRow(row) {
    for (let column = 0; column < this.boardState.columnCount; column++) {
      this.board.appendChild(this.#createLine("lv", row, column));
      this.board.appendChild(this.#createBox(Math.floor(row / 2), column));
    }
    this.board.appendChild(this.#createLine("lv", row, this.boardState.columnCount));
  }

  #createDot() {
    return this.#createDivWithClass("d");
  }

  #createBox(row, column) {
    const box = this.#createDivWithClass("b");

    const boxFillStyle = FILLED_BOX_STYLES[this.boardState.boxFilled(row, column)];
    if (boxFillStyle) {
      box.classList.add("filled", boxFillStyle);
    } else {
        box.dataset.boxIndex = this.boardState.getBoxIndex(row, column);
    }

    return box;
  }

  #createLine(lineTypeClass, row, column) {
    const line = this.#createDivWithClass(lineTypeClass);

    const lineFillStyle = FILLED_LINE_STYLES[this.boardState.lineMarked(row, column)];
    if (lineFillStyle) {
      line.classList.add("filled", lineFillStyle);
    } else {
      line.dataset.row = row;
      line.dataset.column = column;

      line.addEventListener("pointerup", e => markLine(e, row, column));
    }

    return line;
  }

  #createDivWithClass(elementClass) {
    const element = document.createElement("div");
    element.classList.add(elementClass);
    return element;
  }
}

async function markLine(event, row, column) {
  const classes = event.currentTarget.classList;
  if (!classes.contains("filled")) {
    const gameClient = await getGameClient();
    const turnResult = await gameClient.markLine(row, column);
  }
}

function turnCompleted(board, turnResponse) {
  const row = turnResponse.lineRow;
  const column = turnResponse.lineColumn;
  const line = board.querySelector(`div:is(.lh,.lv)[data-row='${row}'][data-column='${column}']`);

  if (line) {
    const lineFillStyle = FILLED_LINE_STYLES[turnResponse.lastPlayer + 1];
    line.classList.add("filled", lineFillStyle);
  } else {
    console.log(`ERROR: No line element found at row ${row} and column ${column}.`);
  }

  if (turnResponse.filledBoxes.length > 0) {
    const boxFillStyle = FILLED_BOX_STYLES[turnResponse.lastPlayer + 1];

    for (const boxIndex of turnResponse.filledBoxes) {
      const box = board.querySelector(`div.b[data-box-index='${boxIndex}']`);
      if (box) {
        box.classList.add("filled", boxFillStyle)
      } else {
        console.log(`ERROR: No box element found with index ${boxIndex}.`);
      }
    }
  }

  checkOutcome(turnResponse.outcome);
}

function getCurrentPlayerId(defaultId) {
  const hash = window.location.hash;
  if (hash.length == 0) {
    return defaultId;
  } else {
    const playerId = hash.substring(1);
    return playerId;
  }
}

async function loadGameState(gameClient, gameId, playerOneId, playerTwoId) {
  const playerId = getCurrentPlayerId(playerOneId);

  try {
    return await gameClient.getGame(gameId, playerId);
  } catch (error) {
    if (error.message.indexOf("404") >= 0) {
      return await gameClient.createGame(1, 1, playerOneId, playerTwoId, playerId);
    } else {
      console.log(error);
      throw error;
    }
  }
}

let checkOutcome = _ => {};

const checkOutcomeForPlayer = playerIndex => outcome => {
  if (outcome) {
    const thisPlayerScore = playerIndex == 0 ? outcome.playerOneScore : outcome.playerTwoScore;
    const otherPlayerScore = playerIndex == 0 ? outcome.playerTwoScore : outcome.playerOneScore;

    let message = "";
    if (thisPlayerScore == otherPlayerScore) {
      message = "Game ended in a TIE!";
    } else if (thisPlayerScore > otherPlayerScore) {
      message = "You are the winner!";
    } else {
      message = "You lost this game."
    }

    message += `\n\nYour Score: ${thisPlayerScore}\nOpponent Score: ${otherPlayerScore}`;
    window.setTimeout(() => window.alert(message), 500);
  }
}

async function initialize() {
  const boardArea = document.querySelector("div.board-area");
  const board = document.querySelector("div.board");
  const gameClient = await getGameClient();
  const gameState = await loadGameState(
    gameClient,
    "A33DFDFF-A3C0-4F7F-B4B2-9664E78D111B",
    "p1", "p2");

  new BoardUIBuilder(gameState, boardArea, board).build();
  gameClient.setOnTurnCompleted(e => turnCompleted(board, e));

  checkOutcome = checkOutcomeForPlayer(gameState.playerIndex);
  checkOutcome(gameState.outcome);
}

window.addEventListener("load", initialize);
