import json

from dotsquares import Board


def handler(event, context):
    board = Board(3, 3)
    board.mark_line(1, 0, 1)
    board.mark_line(5, 2, 2)

    lines_one = board.marked_line_indices(1)
    lines_two = board.marked_line_indices(2)

    return {
        'statusCode': 200,
        'body': json.dumps([lines_one, lines_two])
    }
