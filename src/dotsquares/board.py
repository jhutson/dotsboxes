
# horizontal line - H
# vertical line - V

# ex. 2 rows, 3 columns
#   H H H
#  V-V-V-V
#   H H H 
#  V-V-V-V
#   H H H

import numpy as np

class Board:
    def __init__(self, rows, columns):
        self.lines = np.zeros((2 * rows + 1, columns + 1), dtype=np.byte)
        self.boxes = np.zeros((rows, columns), dtype=np.byte)


    def markLine(self, row: int, column: int, player: int):

        if row % 2 == 0 and column >= self.lines.shape[1]:
            raise IndexError

        if self.lines[row, column] == 0:
            self.lines[row, column] = player
            boxes = self.checkForFilledBoxes(row, column, player)

            for box in boxes:
                self.boxes[box] = player

            return boxes

        return None


    def checkForFilledBoxes(self, lineRow: int, lineColumn: int, player: int):
        filled = []

        if lineRow % 2 == 0: # horizontal line
            # check square above
            if lineRow > 0:
                if self.lines[(lineRow - 2, lineColumn)] > 0:
                    if self.lines[(lineRow - 1, lineColumn)] > 0 and self.lines[(lineRow - 1, lineColumn + 1)] > 0:
                        box = (lineRow // 2 - 1, lineColumn)
                        filled.append(box)

            # check square below
            if lineRow + 1 < self.lines.shape[0]:
                if self.lines[(lineRow + 2, lineColumn)] > 0:
                    if self.lines[(lineRow + 1, lineColumn)] > 0 and self.lines[(lineRow + 1, lineColumn + 1)] > 0:
                        box = (lineRow // 2, lineColumn)
                        filled.append(box)

        else: # vertical line
            # check square to left
            if lineColumn > 0:
                if self.lines[(lineRow, lineColumn - 1)] > 0:
                    if self.lines[(lineRow - 1, lineColumn - 1)] > 0 and self.lines[(lineRow + 1, lineColumn - 1)] > 0:
                        box = ((lineRow - 1) // 2, lineColumn - 1)
                        filled.append(box)

            # check square to right
            if lineColumn + 1 < self.lines.shape[1]:
                if self.lines[(lineRow, lineColumn + 1)] > 0:
                    if self.lines[(lineRow - 1, lineColumn)] > 0 and self.lines[(lineRow + 1, lineColumn)] > 0:
                        box = ((lineRow - 1) // 2, lineColumn)
                        filled.append(box)

        return filled
