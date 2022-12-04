import { testLoad } from './game-client.js';

function populateBoard(boardArea, board) {
  const rowCount = 10;
  const columnCount = 10;
  const player = 1;

  testLoad().then(r => console.log(r));

  boardArea.style.setProperty("--db-column-count", columnCount);
  boardArea.style.setProperty("--db-row-count", rowCount);
  boardArea.style.setProperty("--db-highlight",
    player == 1 ? "var(--db-highlight-one)" : "var(--db-highlight-two)"
  );

  for (let row = 0; row < rowCount * 2 + 1; ++row) {
    if (row % 2 == 0) {
      populateHorizontalRow(board, player, row, columnCount);
    } else {
      populateVerticalRow(board, player, row, columnCount);
    }
  }

  // Example of filling box
  board.querySelector("div.b[data-row='3'][data-column='2']")
    .classList.add("filled", player == 1 ? "bf1" : "bf2");

  board.querySelector("div.b[data-row='1'][data-column='3']")
    .classList.add("filled", player == 1 ? "bf2" : "bf1");
}

function populateHorizontalRow(board, player, row, columnCount) {
  for (let column = 0; column < columnCount; column++) {
    board.appendChild(createDot());
    board.appendChild(createLine("lh", player, row, column));
  }
  board.appendChild(createDot());
}

function populateVerticalRow(board, player, row, columnCount) {
  for (let column = 0; column < columnCount; column++) {
    board.appendChild(createLine("lv", player, row, column));
    board.appendChild(createBox(Math.floor(row / 2), column));
  }
  board.appendChild(createLine("lv", player, row, columnCount));
}

function createDot() {
  return createDivWithClass("d");
}

function createBox(row, column) {
  const box = createDivWithClass("b");
  box.dataset.row = row;
  box.dataset.column = column;
  return box;
}

function createLine(lineTypeClass, player, row, column) {
  const line = createDivWithClass(lineTypeClass);
  line.addEventListener("pointerup", e => markLine(e, player, row, column));
  return line;
}

function createDivWithClass(elementClass) {
  const element = document.createElement("div");
  element.classList.add(elementClass);
  return element;
}

function markLine(event, player, row, column) {
  const classes = event.currentTarget.classList;
  if (classes.contains("filled")) {
    classes.remove("filled", "lf1", "lf2");
  } else {
    const fillTypes = ["lf1", "lf2"];
    let fillIndex = player - 1;

    if (event.shiftKey) {
      fillIndex = 1 - fillIndex;
    }

    const fillType = fillTypes[fillIndex];
    classes.add("filled", fillType);
  }
}

function initialize() {
  const boardArea = document.querySelector("div.board-area");
  const board = document.querySelector("div.board");
  populateBoard(boardArea, board);
}

window.addEventListener("load", initialize);