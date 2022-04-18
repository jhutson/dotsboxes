import pytest
from dotsboxes import Board
from io import BytesIO


def test_mark_line_past_row_raises():
    '''Verify that trying to mark line in column in horizontal (even) row
        is limited to number of boxes in that row.'''
    board = Board(3, 1)

    with pytest.raises(IndexError):
        board.mark_line(0, 1, 1)


@pytest.mark.parametrize("boxShape,expectedLineShape", [
    ((1, 1), (3, 2)),
    ((2, 3), (5, 4))
])
def test_one_box_line_shape(boxShape, expectedLineShape):
    board = Board(*boxShape)
    assert(board.lines.shape == expectedLineShape)


def test_line_marked_once():
    board = Board(1, 1)
    board.mark_line(0, 0, 1)
    assert(board.mark_line(0, 0, 1) == None)


@pytest.mark.parametrize("index", [0, 1, 2, 3])
def test_make_box(index):
    lines = [(0, 0), (1, 1), (2, 0), (1, 0)]
    lines = lines[index:] + lines[:index]

    board = Board(1, 1)

    assert(board.mark_line(*lines[0], 1) == [])
    assert(board.mark_line(*lines[1], 2) == [])
    assert(board.mark_line(*lines[2], 1) == [])
    assert(board.mark_line(*lines[3], 2) == [(0, 0)])


def test_make_two_boxes_from_vertical_line():
    board = Board(1, 2)

    player = 1
    for line in [(0, 0), (0, 1), (1, 0), (1, 2), (2, 0), (2, 1)]:
        assert(board.mark_line(*line, player) == [])
        player = player % 2 + 1

    assert(board.mark_line(1, 1, player) == [(0, 0), (0, 1)])


def test_make_two_boxes_from_horizontal_line():
    board = Board(2, 1)

    player = 1
    for line in [(0, 0), (1, 0), (1, 1), (3, 0), (3, 1), (4, 0)]:
        assert(board.mark_line(*line, player) == [])
        player = player % 2 + 1

    assert(board.mark_line(2, 0, player) == [(0, 0), (1, 0)])


def test_fill_count_zero_player_raises():
    board = Board(1, 1)

    with pytest.raises(ValueError):
        board.fill_count(0)


def test_filled_box_indices_zero_player_raises():
    board = Board(1, 1)

    with pytest.raises(ValueError):
        board.filled_box_indices(0)


def test_filled_box_indices_none():
    board = Board(1, 1)

    assert(board.filled_box_indices(1) == ([], []))


def test_filled_box_indices_some():
    board = Board(3, 3)
    board.boxes[(0, 0)] = 1
    board.boxes[(0, 2)] = 1
    board.boxes[(0, 1)] = 2
    board.boxes[(2, 1)] = 1
    board.boxes[(2, 2)] = 1

    assert(board.filled_box_indices(1) == ([0, 0, 2, 2], [0, 2, 1, 2]))
    assert(board.filled_box_indices(2) == ([0], [1]))
    assert(board.filled_box_indices(3) == ([], []))


def test_marked_line_indices_zero_player_raises():
    board = Board(1, 1)

    with pytest.raises(ValueError):
        board.marked_line_indices(0)


def test_marked_line_indices_none():
    board = Board(1, 1)

    assert(board.marked_line_indices(1) == ([], []))


def test_marked_line_indices_some():
    board = Board(4, 3)

    board.lines[(1, 0)] = 1
    board.lines[(1, 2)] = 2
    board.lines[(3, 1)] = 1
    board.lines[(3, 2)] = 1
    board.lines[(2, 2)] = 2

    assert(board.marked_line_indices(1) == ([1, 3, 3], [0, 1, 2]))
    assert(board.marked_line_indices(2) == ([1, 2], [2, 2]))
    assert(board.marked_line_indices(3) == ([], []))


def test_save_and_load_board():
    board = Board(6, 5)

    board.mark_line(0, 0, 1)
    board.mark_line(1, 0, 2)
    board.mark_line(1, 1, 1)
    assert(board.mark_line(2, 0, 2) == [(0, 0)])

    board.mark_line(2, 2, 2)
    board.mark_line(3, 2, 1)
    board.mark_line(3, 3, 2)
    assert(board.mark_line(4, 2, 1) == [(1, 2)])

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
