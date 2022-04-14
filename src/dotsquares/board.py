
# odds - H
# evens - V

# 3 column, 2 rows
#   H H H
#  V V V V
#   H H H 
#  V V V V
#   H H H

# m columns x n rows
# m Hs per column for a row
# m + 1 Vs per column for a row
# n V rows
# n + 1 H rows
# m(n+1) + n(m+1) total H and V items
# f(m,n) = m(n+1) + n(m+1)

# m = 3, n = 2
# 3(2+1) + 2(3+1) 
# 3(3) + 2(4)
# 9 + 8
# 17 yes

# 30x30, m = n = 30
# f(m,n) = m(n+1) + n(m+1)
# f(30,30) = 30(30+1) + 30(30+1)
#  = 2(30(31))
#  = 1860

# total storage for lines (above) and boxes:
# t(m,n) = f(m,n) + mn
#        = m(n+1) + n(m+1) + mn
#        = mn + m + mn + n + mn
#        = 3mn + m + n
# t(30,30) = 3(30)(30) + 30 + 30
# t(30,30) = 3(30)(30) + 2(30)
# t(30,30) = 30(3(30) + 2)
# t(30,30) = 30(92)
# t(30,30) = 2760

import numpy as np

class Board:
    def __init__(self, columns, rows):
        self.lines = np.zeros((columns + 1, rows + 1), dtype=np.bool_)
        self.boxes = np.zeros((columns, rows), dtype=np.bool_)
