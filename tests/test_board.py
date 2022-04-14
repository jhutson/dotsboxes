from dotsquares import Board

def test_first():
    board = Board(3,2)
    assert board.boxes.shape == (3,2)
