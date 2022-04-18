from .board import Board


class TurnResult:

    def __init__(self, current_player: int, last_player: int, last_filled: list = []):
        self.current_player = current_player
        self.last_player = last_player
        self.last_filled = last_filled

    def __eq__(self, other: object) -> bool:
        if isinstance(other, TurnResult):
            return (self.current_player == other.current_player and
                    self.last_player == other.last_player and
                    self.last_filled == other.last_filled)

        return False

    def __str__(self) -> str:
        return (f'TurnResult(current_player={self.current_player}, '
                f'last_player={self.last_player}, '
                f'last_filled={self.last_filled})'
                )


class Game:

    def __init__(self, board: Board, player_count: int, current_player=1):
        if player_count < 0 or player_count > 4:
            raise ValueError(
                'player_count must be greater than zero and no more than four.')

        self.board = board
        self.player_count = player_count
        self.current_player = current_player

    def mark_line(self, row: int, column: int):
        last_player = self.current_player
        filled = self.board.mark_line(row, column, self.current_player)

        if filled is not None and not filled:
            self.current_player = self.current_player % self.player_count + 1

        return TurnResult(self.current_player, last_player, filled)

    def outcome(self):
        if self.board.has_open_boxes():
            return []

        scores = [(player, self.board.fill_count(player))
                  for player in range(1, self.player_count + 1)]
        scores.sort(key=lambda s: s[1], reverse=True)

        return scores
