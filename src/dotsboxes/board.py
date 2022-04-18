
# horizontal line - H
# vertical line - V

# ex. 2 rows, 3 columns
#   H H H
#  V-V-V-V
#   H H H
#  V-V-V-V
#   H H H

import numpy as np
from typing import BinaryIO


class Board:
    def __init__(self, rows: int, columns: int):
        if rows == 0 and columns == 0:
            self.lines = None
            self.boxes = None
        else:
            self.lines = np.zeros((2 * rows + 1, columns + 1), dtype=np.byte)
            self.boxes = np.zeros((rows, columns), dtype=np.byte)

    def mark_line(self, row: int, column: int, player: int):

        if row % 2 == 0 and column + 1 == self.lines.shape[1]:
            raise IndexError

        if self.lines[row, column] == 0:
            self.lines[row, column] = player
            boxes = self.check_for_filled_boxes(row, column, player)

            for box in boxes:
                self.boxes[box] = player

            return boxes

        return None

    def check_for_filled_boxes(self, lineRow: int, lineColumn: int, player: int):
        filled = []

        if lineRow % 2 == 0:  # horizontal line
            # check box above
            if lineRow > 0:
                if self.lines[(lineRow - 2, lineColumn)] > 0:
                    if self.lines[(lineRow - 1, lineColumn)] > 0 and self.lines[(lineRow - 1, lineColumn + 1)] > 0:
                        box = (lineRow // 2 - 1, lineColumn)
                        filled.append(box)

            # check box below
            if lineRow + 1 < self.lines.shape[0]:
                if self.lines[(lineRow + 2, lineColumn)] > 0:
                    if self.lines[(lineRow + 1, lineColumn)] > 0 and self.lines[(lineRow + 1, lineColumn + 1)] > 0:
                        box = (lineRow // 2, lineColumn)
                        filled.append(box)

        else:  # vertical line
            # check box to left
            if lineColumn > 0:
                if self.lines[(lineRow, lineColumn - 1)] > 0:
                    if self.lines[(lineRow - 1, lineColumn - 1)] > 0 and self.lines[(lineRow + 1, lineColumn - 1)] > 0:
                        box = ((lineRow - 1) // 2, lineColumn - 1)
                        filled.append(box)

            # check box to right
            if lineColumn + 1 < self.lines.shape[1]:
                if self.lines[(lineRow, lineColumn + 1)] > 0:
                    if self.lines[(lineRow - 1, lineColumn)] > 0 and self.lines[(lineRow + 1, lineColumn)] > 0:
                        box = ((lineRow - 1) // 2, lineColumn)
                        filled.append(box)

        return filled

    def has_open_boxes(self):
        return not self.boxes.all()

    def fill_count(self, player: int) -> int:
        if player > 0:
            return np.sum(self.boxes == player)

        raise ValueError("player must be greater than zero.")

    def filled_box_indices(self, player: int):
        return self._get_indices(player, self.boxes)

    def marked_line_indices(self, player: int):
        return self._get_indices(player, self.lines)

    def _get_indices(self, player: int, items: np.ndarray):
        if player > 0:
            indices = (items == player).nonzero()
            row_indices = indices[0].tolist()
            column_indices = indices[1].tolist()

            return (row_indices, column_indices)

        raise ValueError("player must be greater than zero.")

    def save(self, file: BinaryIO):
        np.savez_compressed(file, lines=self.lines, boxes=self.boxes)

    @classmethod
    def load(cls, file: BinaryIO):
        with np.load(file) as data:
            if 'lines' in data.files and 'boxes' in data.files:
                board = Board(0, 0)
                board.lines = data['lines']
                board.boxes = data['boxes']

                return board

        raise ValueError(
            'Specified file does not contain a valid board state.')
