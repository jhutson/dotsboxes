import pytest
from dotsquares import Board
from io import BytesIO

@pytest.mark.parametrize("boxShape,expectedLineShape", [
    ((1, 1), (3, 2)),
    ((2, 3), (5, 4))
])
def test_one_square_line_shape(boxShape, expectedLineShape):
    board = Board(*boxShape)
    assert(board.lines.shape == expectedLineShape)


def test_line_marked_once():
    board = Board(1, 1)
    board.markLine(0, 0, 1)
    assert(board.markLine(0, 0, 1) == None)


@pytest.mark.parametrize("index", [0, 1, 2, 3])
def test_make_square(index):
    lines = [(0, 0), (1, 1), (2, 0), (1, 0)]
    lines = lines[index:] + lines[:index]

    board = Board(1, 1)

    assert(board.markLine(*lines[0], 1) == [])
    assert(board.markLine(*lines[1], 2) == [])
    assert(board.markLine(*lines[2], 1) == [])
    assert(board.markLine(*lines[3], 2) == [(0,0)])


def test_make_two_squares_from_vertical_line():
    board = Board(1, 2)

    player = 1
    for line in [(0, 0), (0, 1), (1, 0), (1, 2), (2, 0), (2, 1)]:
        assert(board.markLine(*line, player) == [])
        player = player % 2 + 1

    assert(board.markLine(1, 1, player) == [(0, 0), (0, 1)])


def test_make_two_squares_from_horizontal_line():
    board = Board(2, 1)

    player = 1
    for line in [(0, 0), (1, 0), (1, 1), (3, 0), (3, 1), (4, 0)]:
        assert(board.markLine(*line, player) == [])
        player = player % 2 + 1

    assert(board.markLine(2, 0, player) == [(0, 0), (1, 0)])


def test_save_and_load_board():
    board = Board(6, 5)

    board.markLine(0, 0, 1)
    board.markLine(1, 0, 2)
    board.markLine(1, 1, 1)
    assert(board.markLine(2, 0, 2) == [(0, 0)])

    board.markLine(2, 2, 2)
    board.markLine(3, 2, 1)
    board.markLine(3, 3, 2)
    assert(board.markLine(4, 2, 1) == [(1, 2)])

    board_two: Board = None

    with BytesIO() as buffer:
        board.save(buffer)
        buffer.seek(0)

        board_two = Board.load(buffer)

    assert(board_two.lines[(0, 0)] == 1)
    assert(board_two.lines[(1, 0)] == 2)
    assert(board_two.lines[(1, 1)] == 1)
    assert(board_two.lines[(2, 0)] == 2)

    assert(board_two.lines[(2, 2)] == 2)
    assert(board_two.lines[(3, 2)] == 1)
    assert(board_two.lines[(3, 3)] == 2)
    assert(board_two.lines[(4, 2)] == 1)
    
    assert(board_two.boxes[(0, 0)] == 2)
    assert(board_two.boxes[(1, 2)] == 1)



