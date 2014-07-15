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

class Cell {
  int gameNo, nMatches, xPos, yPos, xSize, ySize, minLeft, secLeft;
  boolean isActive, isTicking, miniButtons, playPressed, skipPressed;
  TableRow fixture;
  PFont cellTeamFont, cellInfoFontSmall, cellInfoFontLarge;
  PImage playIcon, pauseIcon, skipIcon, stopIcon;
  IconButton playButton, skipButton;

  Cell(int gameNo_, TableRow fixture_, int xPos_, int yPos_, int xSize_, int ySize_) {
    gameNo = gameNo_;
    xPos = xPos_;
    yPos = yPos_;
    xSize = xSize_;
    ySize = ySize_;

    isActive = false;
    isTicking = false;
    miniButtons = false;
    skipPressed = false;
    playPressed = false;

    fixture = fixture_;
    nMatches = fixture.getColumnCount()/2;

    // Load assets
    cellTeamFont = loadFont("fonts/SquarishSansCTRegular-66.vlw");
    cellInfoFontSmall = loadFont("fonts/SquarishSansCTRegular-24.vlw");
    cellInfoFontLarge = loadFont("fonts/SquarishSansCTRegular-48.vlw");
    playIcon = loadImage("images/play.png");
    pauseIcon = loadImage("images/pause.png");
    skipIcon = loadImage("images/skip.png");
    stopIcon = loadImage("images/stop.png");

    // Create buttons
    if (miniButtons) {
      playButton = new IconButton(xPos+xSize-266, yPos+ySize-63, 48, 48, playIcon, TIMER_GREEN, TIMER_GREY);
      skipButton = new IconButton(xPos+xSize-204, yPos+ySize-63, 48, 48, skipIcon, TIMER_RED, TIMER_GREY);
    } else {
      playButton = new IconButton(xPos+98, yPos+ySize-63, xSize/2-134, 48, playIcon, TIMER_GREEN, TIMER_GREY);
      skipButton = new IconButton(xPos+xSize/2-22, yPos+ySize-63, xSize/2-134, 48, skipIcon, TIMER_RED, TIMER_GREY);
    }
  }  

  void display() {
    // Fill cell
    for (int i = 0; i < ySize; i++) {
      float fillColour = map(i, 0, ySize, (isActive ? 200 : 60), (isActive ? 180 : 50));
      fill(fillColour);
      rect(xPos, yPos+i, xSize, 1);
    }

    // Draw outline and dividing lines
    noFill();
    stroke(0);
    strokeWeight(1.5);
    rect(xPos, yPos, xSize, ySize);
    for (int i = 1; i < nMatches; i++) {
      line(xPos+i*xSize/nMatches, yPos+35, xPos+i*xSize/nMatches, yPos+ySize/2-18);
    } 
    noStroke();

    // Display playing teams
    for (int i = 0; i < nMatches; i++) {
      int xCentre = xPos+(2*i+1)*xSize/(2*nMatches);
      int leftTeam = fixture.getInt(2*i);
      int rightTeam = fixture.getInt(2*i+1);

      // Optimise text centering
      if (leftTeam%10 == 1) { 
        xCentre -= 7;
      }
      if (floor(leftTeam/10.0) == 1) {
        xCentre -= 8;
      }
      if (rightTeam%10 == 1) {
        xCentre += 8;
      }
      if (leftTeam < 10 && floor(rightTeam/10.0) == 1) { 
        xCentre -= 20;
      } else if (leftTeam < 10 && floor(rightTeam/10.0) > 1) { 
        xCentre -= 25;
      } 
      if (floor(leftTeam/10.0) == 1 && floor(rightTeam/10.0) == 1) {
        xCentre += 8;
      }

      // Draw highlight box
      fill(TIMER_GREY, isActive ? 40 : 0);
      if (isActive && minLeft == 0 && secLeft > 10 && secLeft <= 30 && secLeft%2 == 1) {
        // Flash amber if 10 to 30 seconds remaining
        fill(TIMER_AMBER, 200);
      } else if (isActive && minLeft == 0 && secLeft <= 10 && secLeft%2 == 1) {
        // Flash red if less than 10 seconds remaining
        fill(TIMER_RED, 180);
      }
      rect(xPos+14+i*xSize/nMatches, yPos+14, xSize/nMatches-28, ySize/2-10, 10);

      // Print centered playing teams (ternary operators kern '1v' and 'v1')
      fill(isActive ? TIMER_GREY : 30);
      textFont(cellTeamFont);
      textAlign(CENTER);
      text("v", xCentre+1, yPos+70);
      textAlign(RIGHT);
      text(leftTeam, xCentre-(leftTeam%10 == 1 ? 20 : 25), yPos+70);
      textAlign(LEFT);
      text(rightTeam, xCentre+(floor(rightTeam/10.0) == 1 ? 14 : 25), yPos+70);
    }

    // Display game and time information
    if (isActive) {
      fill(TIMER_GREY);
      textAlign(LEFT);
      textFont(cellInfoFontSmall);
      text("Game", xPos+15, yPos+ySize-50);
      textFont(cellInfoFontLarge);
      String gameString = String.format("%02d", gameNo%100);
      text(gameString, xPos+14, yPos+ySize-14);
      textAlign(RIGHT);
      textFont(cellInfoFontSmall);
      text("Remaining", xPos+xSize-17, yPos+ySize-50);
      textFont(cellInfoFontLarge);
      String secString = String.format("%02d", secLeft);
      text(secString, xPos+xSize-14, yPos+ySize-14);
      textAlign(LEFT);
      text(minLeft, xPos+xSize-145, yPos+ySize-14);
      text(":", xPos+xSize-106, yPos+ySize-14);
    }

    // Display game controls
    if (isActive) {
      playButton.display();
      skipButton.display();
    }
  }  

  void respond(int clickX, int clickY) {
    // Check for button presses
    playButton.respond(clickX, clickY);
    skipButton.respond(clickX, clickY);

    // Flag results
    if (playButton.wasPressed) {
      playButton.wasPressed = false;
      playPressed = true;
    } else if (skipButton.wasPressed) {
      skipButton.wasPressed = false;
      skipPressed = true;
    }
  }

  void setXPos(int newPos) {
    xPos = newPos;
    updateButtons();
  }

  void setYPos(int newPos) { 
    yPos = newPos;
    updateButtons();
  }

  void setFixture(TableRow newFixture) {
    fixture = newFixture;
    nMatches = fixture.getColumnCount()/2;
  }

  void setGameNo(int newNo) {
    gameNo = newNo;
  }

  void setHeight(int newSize) {
    ySize = newSize;
    updateButtons();
  }

  void setMinLeft(int newMin) {
    minLeft = newMin;
  }

  void setSecLeft(int newSec) {
    secLeft = newSec;
  }

  void setWidth(int newSize) {
    xSize = newSize;
    updateButtons();
  }

  void updateButtons() {
    // Resize and reposition
    if (miniButtons) {
      playButton.resizeTo(xPos+xSize-266, yPos+ySize-63, 48, 48);
      skipButton.resizeTo(xPos+xSize-204, yPos+ySize-63, 48, 48);
    } else {
      playButton.resizeTo(xPos+98, yPos+ySize-63, xSize/2-134, 48);
      skipButton.resizeTo(xPos+xSize/2-22, yPos+ySize-63, xSize/2-134, 48);
    }

    // Set icon and highlight colour
    if (isTicking) {
      playButton.setIcon(pauseIcon);
      playButton.setActiveColour(TIMER_AMBER);
      skipButton.setIcon(stopIcon);
    } else {
      playButton.setIcon(playIcon);
      playButton.setActiveColour(TIMER_GREEN);
      skipButton.setIcon(skipIcon);
    }
  }
}

