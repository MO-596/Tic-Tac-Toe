package com.example.tic_tac_toeapplication;

public class TicTacToe {
    public static final int SIDE = 3;
    private int turn;// 1 = X, 2 = O
    private int [][] gameBoard;
    private int[][] winTriplet = null;  // stores the winning line when game ends

    public TicTacToe()
    {
      gameBoard = new int[SIDE][SIDE];
      resetGame();
    }

    public int Play(int row, int col)
    {
      int currentTurn = turn;
      if(row >= 0 && col >= 0 && row < SIDE && col < SIDE && gameBoard[row][col] == 0)
      {
       gameBoard[row][col] = turn;
       turn = (turn == 1) ? 2 : 1;

        return currentTurn;
      }
      else
      {
        return 0;
      }
    }

    public void resetGame()
    {
        for(int row = 0; row < SIDE; row++)
        {
            for(int col = 0; col < SIDE; col++)
            {
                gameBoard[row][col]=0;
            }
        }
        turn = 1;
        winTriplet = null;// ← NEW
    }

    public boolean isGameOver()
    {
      return cannotPlay() || (whoWon() > 0);
    }

    public boolean cannotPlay() {
        for (int[] row : gameBoard)
        {
            for (int cell : row)
            {
                if (cell == 0){
                    return false;
                }
            }
        }
        return true;
    }
    /* -------------------------------------------------- win checks */
    public int whoWon()
    {
      int rows = checkRows();
      int columns = checkColumns();
      int diagonals = checkDiagonals();

      if(rows > 0)
      {
        return rows;
      }

      if(columns > 0)
      {
        return columns;
      }

      if(diagonals > 0)
      {
        return diagonals;
      }
      return 0;
    }

    protected int checkRows()
    {
      for (int row = 0; row < SIDE; row++)
      {
        if(gameBoard[row][0] != 0 && gameBoard[row][0] == gameBoard[row][1] && gameBoard[row][1] == gameBoard[row][2])
        {
            winTriplet = new int[][]{{row,0},{row,1},{row,2}};
            return gameBoard[row][0];
        }
      }
      return 0;
    }

    protected int checkColumns()
    {
      for (int col = 0; col < SIDE; col++)
      {
        if(gameBoard[0][col] != 0 && gameBoard[0][col] == gameBoard[1][col] && gameBoard[1][col] == gameBoard[2][col])
        {
            winTriplet = new int[][]{{0,col},{1,col},{2,col}};
            return gameBoard[0][col];
        }
      }
      return 0;
    }

    protected int checkDiagonals()
    {
      if(gameBoard[0][0] != 0 && gameBoard[0][0] == gameBoard[1][1] && gameBoard[1][1] == gameBoard[2][2])
      {
          winTriplet = new int[][]{{0,0},{1,1},{2,2}};
          return gameBoard[0][0];
      }
      if(gameBoard[0][2] != 0 && gameBoard[0][2] == gameBoard[1][1] && gameBoard[1][1] == gameBoard[2][0])
      {
          winTriplet = new int[][]{{0,2},{1,1},{2,0}};
          return gameBoard[2][0];
      }
      return 0;
    }

    /** @return true when the next move belongs to X (player 1). */
    public boolean isXTurn() {
        return turn == 1;
    }

    /** @return 3-element array of winning cells, e.g. {{0,0},{0,1},{0,2}}.
     *  Returns {@code null} if no winner yet or it’s a draw. */
    public int[][] getWinTriplet() {
        return winTriplet;          // assumes you set this when you detect a win
    }
}
