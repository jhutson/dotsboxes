import pytest
from dotsquares import Board

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