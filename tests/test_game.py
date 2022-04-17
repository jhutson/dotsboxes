import json
from dotsquares import Board, Game, TurnResult


def test_mark_line_one():
    game = Game(Board(1, 1), 2)

    assert(game.mark_line(0, 0) == TurnResult(2, 1))


def test_mark_line_twice_no_player_change():
    game = Game(Board(1, 1), 2)
    assert(game.mark_line(0, 0) == TurnResult(2, 1))
    assert(game.mark_line(0, 0) == TurnResult(2, 2, None))


def test_mark_line_for_each_player():
    game = Game(Board(1, 1), 2)
    assert(game.mark_line(0, 0) == TurnResult(2, 1))
    assert(game.mark_line(1, 0) == TurnResult(1, 2))


def test_mark_line_box_filled_goes_again():
    board = Board(1, 2)
    board.mark_line(0, 0, 1)
    board.mark_line(1, 0, 2)
    board.mark_line(2, 0, 1)

    game = Game(board, 2, 2)
    assert(game.mark_line(1, 1) == TurnResult(2, 2, [(0, 0)]))


def test_outcome_open_boxes():
    game = Game(Board(1, 1), 2)
    assert(game.outcome() == [])


def test_outcome_one_winner():
    board = Board(1, 1)
    board.mark_line(0, 0, 1)
    board.mark_line(1, 0, 2)
    board.mark_line(2, 0, 1)
    board.mark_line(1, 1, 2)

    game = Game(board, 2, 2)
    assert(game.outcome() == [(2, 1), (1, 0)])
