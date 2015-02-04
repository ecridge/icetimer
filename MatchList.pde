//    
//    IceTimer - Graphical timer and match scheduler for pick-up ice hockey
//    Copyright (C) 2014  Joe Cridge
//
//    This file is part of IceTimer.
//    
//    IceTimer is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//    
//    IceTimer is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//    
//    You should have received a copy of the GNU General Public License
//    along with IceTimer.  If not, see <http://www.gnu.org/licenses/>.
//    
//    Joe Cridge, June 2014.
//    <joe.cridge@me.com>
//

class MatchList {
  // Preset values
  final int[] LINE_PADDINGS = {
    20, 20, 16, 10
  };
  final int[] CELL_WIDTHS = {
    364, 364, 600, 900
  };
  final int CELL_HEIGHT = 175;
  final int LIST_HEIGHT = 700;

  int cellWidth, cellHeight, linePadding, xStart, yStart, listLength, matchLength, currentGame;
  int startMillis, millisElapsed, millisRemaining;
  float progress;
  boolean isActive, inGame;
  Table matches;
  Cell[] cells;

  MatchList(int nSimGames_, int matchLength_) {
    // Store match length from preferences
    matchLength = matchLength_;

    // Determine cell dimensions
    cellWidth = CELL_WIDTHS[nSimGames_];
    cellHeight = CELL_HEIGHT;
    listLength = ceil((height-190.0)/CELL_HEIGHT) + 1;
    linePadding = LINE_PADDINGS[nSimGames_];   
    xStart = (width-cellWidth)/2;
    yStart = (150+LIST_HEIGHT-40)/2 - cellHeight/2;

    // Initialise
    currentGame = 1;
    isActive = false;
    inGame = false;
    millisRemaining = matchLength * 1000;

    // Create and fill cells with blank data for now
    matches = new Table();
    cells = new Cell[listLength];
    for (int i = 0; i < listLength; i++) {
      cells[i] = new Cell(i, matches.getRow(i), xStart, yStart+(i-1)*cellHeight, cellWidth, cellHeight);
    }
  }

  void activate(int nSimGames_) {
    currentGame = 1;
    isActive = true;

    // Reset and reshape cells
    cellWidth = CELL_WIDTHS[nSimGames_];
    linePadding = LINE_PADDINGS[nSimGames_]; 
    xStart = (width-cellWidth)/2;
    for (int i = 0; i < listLength; i++) {
      cells[i].isActive = false;
      cells[i].setXPos(xStart);
      cells[i].setWidth(cellWidth);
    }

    // Highlight first match
    cells[1].isActive = true;

    // Initialise match clock
    cells[1].setMinLeft(floor(matchLength/60));
    cells[1].setSecLeft(matchLength % 60);
  }


  void advanceGame(int step) {
    inGame = false;

    // Reset selected cell
    cells[1].isTicking = false;
    cells[1].updateButtons();
    cells[1].setMinLeft(floor(matchLength/60));
    cells[1].setSecLeft(matchLength % 60);

    // Reset timing data
    millisRemaining = matchLength * 1000;
    millisElapsed = 0;
    progress = 0.0;

    // Realign cells with 'now' line
    offsetCells(progress);

    currentGame += step;
    if (currentGame < 0) {
      currentGame += 100;
    }
    populateCells();
  }

  void display() {
    // Draw 'now' line
    if (isActive) {
      // Blink line in last 10 seconds
      int secLeft = floor(millisRemaining/1000.0);
      if (secLeft <= 10 && (secLeft%60)%2 == 1) {
        fill(100);
      } else {
        fill(200);
      }
      rect(linePadding, yStart-1, xStart-2*linePadding, 2);
      rect(xStart+cellWidth+linePadding, yStart-1, xStart-2*linePadding, 2);

      // Same for line shadow
      if (secLeft <= 10 && (secLeft%60)%2 == 1) {
        fill(0);
      } else {
        fill(100);
      }
      rect(linePadding, yStart+1, xStart-2*linePadding, 2);
      rect(xStart+cellWidth+linePadding, yStart+1, xStart-2*linePadding, 2);
    } else {
      // Display solid dark line when not in session
      fill(100);
      rect(linePadding, yStart-1, xStart-2*linePadding, 2);
      rect(xStart+cellWidth+linePadding, yStart-1, xStart-2*linePadding, 2);
      fill(0);
      rect(linePadding, yStart+1, xStart-2*linePadding, 2);
      rect(xStart+cellWidth+linePadding, yStart+1, xStart-2*linePadding, 2);
    }

    // Draw shadow
    for (int i = 0; i < 6; i++) {
      fill(0, 0, 0, 90-15*i);
      rect(xStart+cellWidth+i, 150, 1, height-190);
      rect(xStart-i, 150, 1, height-190);
    }

    // Dsiplay cells
    for (int i = 0; i < listLength; i++) {
      cells[i].display();
    }
  }

  void deactivate() {
    // End game and tidy up
    advanceGame(1);

    // Clear cells
    matches = new Table();
    for (int i = 0; i < listLength; i++) {
      cells[i].isActive = false;
      cells[i].isTicking = false;
      cells[i].setFixture(matches.getRow(i));
    }

    isActive = false;
  }

  void offsetCells(float percentage) {
    // Scrolls cells upwards as game progresses
    for (int i = 0; i < listLength; i++) {
      int offsetYPos = round(yStart+(i-1)*cellHeight-percentage*cellHeight);
      cells[i].setYPos(offsetYPos);
    }
  }

  void pauseGame() {
    inGame = false;
    cells[1].isTicking = false;
    cells[1].updateButtons();
  }
  
  Table permuteTableRows(Table inTable, int nSimGames) {
    // Creates and returns a new table based on one read from a CSV file, but with its rows permuted by random amounts.
    
    // Note: This is not pretty! The Table class is horrible and passed by reference hence the fiddly order of operations here.
    // Updating the whole program to work with arrays rather than Tables would be a good idea!
    
    // Copy first row as is (and create the necessary column structure)
    Table outTable = new Table();
    outTable.addRow();
    for (int j = 0; j < nSimGames*2; j++) {
      outTable.addColumn();
      outTable.setInt(0, j, inTable.getInt(0, j));
    }
    
    // Copy the remaining rows with random permutations
    for (int i = 1; i < 50; i++) {
      outTable.addRow();
      int offset = 2*int(random(nSimGames));
      for (int j = 0; j < nSimGames*2; j++) {
        outTable.setInt(i, j, inTable.getInt(i, (j+offset)%(nSimGames*2)));
      }
    }
    return outTable;
  }

  void playPause() {
    // Interprets spacebar press
    if (isActive) {
      // Start or pause game
      if (inGame) {
        pauseGame();
      } else {
        startGame();
      }
    }
  }

  void populateCells() {
    // Fills cells with match data
    for (int i = 0; i < listLength; i++) {
      // Loop indeces round
      int idx = (i+currentGame-2)%50 < 0 ? (i+currentGame-2)%50+50 : (i+currentGame-2)%50;
      cells[i].setFixture(matches.getRow(idx));
      cells[i].setGameNo(i+currentGame-1);
    }
  }

  void reloadMatches(int numTeams, int numSimGames) {
    // Loads new match list from file and repopulates cells
    String fixtureFile = "nsim-nteams/" + numSimGames + "-" + numTeams + ".csv";
    Table tempTable = loadTable(fixtureFile);
    matches = permuteTableRows(tempTable, numSimGames);
    populateCells();
  }

  void respond(int clickX, int clickY) {
    // Passes click to active cell and reacts
    if (isActive) {
      // Only central cell is active so don't need to check others
      cells[1].respond(clickX, clickY);
      if (cells[1].playPressed) {
        cells[1].playPressed = false;
        // Start or pause game
        if (inGame) {
          pauseGame();
        } else {
          startGame();
        }
      } else if (cells[1].skipPressed) {
        cells[1].skipPressed = false;
        // End game
        advanceGame(1);
      }
    }
  }

  void startGame() {
    inGame = true;
    cells[1].isTicking = true;
    cells[1].updateButtons();

    if (millisElapsed > 0) {
      // Resume current game
      startMillis = millis() - millisElapsed;
    } else {
      // Begin new game
      startMillis = millis();
      progress = 0.0;
      millisElapsed = 0;
      millisRemaining = matchLength * 1000;
    }
  }

  void toggleMiniButtons() {
    // Toggles between wide and mini (square) match buttons
    boolean newSetting = !(cells[0].miniButtons);
    for (int i = 0; i < listLength; i++) {
      cells[i].miniButtons = newSetting;
      cells[i].updateButtons();
    }
  }

  void update() {
    // Update match timer during game
    if (isActive && inGame && millisRemaining > 0) {
      millisElapsed = millis() - startMillis;
      progress = (1.0 * millisElapsed) / (matchLength * 1000.0);

      // Calculate remaining play time
      millisRemaining = (matchLength * 1000) - millisElapsed;
      if (millisRemaining < 0) {
        millisRemaining = 0;
      }

      // Update clock display
      int minRemaining = floor(millisRemaining/(60.0*1000.0));
      int secRemaining = floor(millisRemaining/1000.0) % 60;
      cells[1].setMinLeft(minRemaining);
      cells[1].setSecLeft(secRemaining);

      // Scroll cells upwards
      offsetCells(progress);
    } else if (isActive && inGame) {
      // End game
      advanceGame(1);
    }
  }
}

