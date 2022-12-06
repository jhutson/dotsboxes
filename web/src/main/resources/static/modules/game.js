import { getGameClient } from './game-client.js';

const filledLineStyles = [null, 'lf1', 'lf2'];
const filledBoxStyles = [null, 'bf1', 'bf2'];

function getCurrentPlayer() {
  const hash = window.location.hash;
  if (hash.length == 0) {
    return "p1";
  } else {
    const playerId = hash.substring(1);
    return playerId;
  }
}

async function populateBoard(boardArea, board) {
  const gameClient = await getGameClient();
  const playerId = getCurrentPlayer();

  let gameState = null;

  try {
    gameState = await gameClient.getGame("A33DFDFF-A3C0-4F7F-B4B2-9664E78D111B", playerId);
  } catch (error) {
    console.log(error);

    if (error.message.indexOf("404") >= 0) {
      gameState = await gameClient.createGame(4, 4, "p1", "p2", playerId === "p1");
    } else {
      return;
    }
  }

  const boardState = gameState.board;
  const rowCount = boardState.rowCount;
  const columnCount = boardState.columnCount;
  const player = playerId === "p1" ? 1 : 2;

  boardArea.style.setProperty("--db-column-count", columnCount);
  boardArea.style.setProperty("--db-row-count", rowCount);
  boardArea.style.setProperty("--db-highlight",
    player == 1 ? "var(--db-highlight-one)" : "var(--db-highlight-two)"
  );

  for (let row = 0; row < rowCount * 2 + 1; ++row) {
    if (row % 2 == 0) {
      populateHorizontalRow(board, boardState, player, row, columnCount);
    } else {
      populateVerticalRow(board, boardState, player, row, columnCount);
    }
  }

  // Example of filling box
//  board.querySelector("div.b[data-row='3'][data-column='2']")
//    .classList.add("filled", player == 0 ? "bf1" : "bf2");
}

function populateHorizontalRow(board, boardState, player, row, columnCount) {
  for (let column = 0; column < columnCount; column++) {
    board.appendChild(createDot());
    board.appendChild(createLine("lh", boardState, player, row, column));
  }
  board.appendChild(createDot());
}

function populateVerticalRow(board, boardState, player, row, columnCount) {
  for (let column = 0; column < columnCount; column++) {
    board.appendChild(createLine("lv", boardState, player, row, column));
    board.appendChild(createBox(boardState, Math.floor(row / 2), column));
  }
  board.appendChild(createLine("lv", boardState, player, row, columnCount));
}

function createDot() {
  return createDivWithClass("d");
}

function createBox(boardState, row, column) {
  const box = createDivWithClass("b");
  box.dataset.row = row;
  box.dataset.column = column;

  const boxFillStyle = filledBoxStyles[boardState.boxFilled(row, column)];
  if (boxFillStyle) {
    box.classList.add("filled", boxFillStyle);
  }

  return box;
}

function createLine(lineTypeClass, boardState, player, row, column) {
  const line = createDivWithClass(lineTypeClass);
  line.addEventListener("pointerup", e => markLine(e, player, row, column));

  const lineFillStyle = filledLineStyles[boardState.lineMarked(row, column)];
  if (lineFillStyle) {
    line.classList.add("filled", lineFillStyle);
  }

  return line;
}

function createDivWithClass(elementClass) {
  const element = document.createElement("div");
  element.classList.add(elementClass);
  return element;
}

async function markLine(event, player, row, column) {
  const classes = event.currentTarget.classList;
  if (!classes.contains("filled")) {
    const gameClient = await getGameClient();
    const turnResult = await gameClient.markLine(row, column);

    const lineFillStyle = filledLineStyles[player];
    classes.add("filled", lineFillStyle);
  }
}

function initialize() {
  const boardArea = document.querySelector("div.board-area");
  const board = document.querySelector("div.board");
  populateBoard(boardArea, board);
}

window.addEventListener("load", initialize);