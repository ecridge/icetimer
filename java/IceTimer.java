import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.Calendar; 
import javax.swing.ImageIcon; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class IceTimer extends PApplet {

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

final int TIMER_GREY = color(60);
final int TIMER_GREEN = color(0, 175, 0);
final int TIMER_AMBER = color(220, 130, 0);
final int TIMER_RED = color(195, 0, 0);

boolean inSession, fullScreen;
int highlight;

ControlPanel controlPanel;
SessionBar sessionBar;
MatchList matchList;

public void setup() {
  // Initialise global variables
  inSession = false;
  highlight = TIMER_GREEN;

  // Load preferences
  Table prefs = loadTable("prefs.csv");
  int halfIceThresh = prefs.getInt(1, 1);
  int thirdIceThresh = prefs.getInt(2, 1);
  int endHour = prefs.getInt(3, dayNumber());
  int endMin = prefs.getInt(4, dayNumber());
  int nTeams = prefs.getInt(5, 1);
  int matchLength = prefs.getInt(6, 1);
  int nSim;
  if (nTeams < halfIceThresh) {
    nSim = 1;
  } else if (nTeams < thirdIceThresh) {
    nSim = 2;
  } else {
    nSim = 3;
  }
  fullScreen = PApplet.parseBoolean(prefs.getString(7, 1));

  // Initialise frame
  if (fullScreen) {
    size(displayWidth, displayHeight);
  } else {
    size(1000, 700);
  }
  background(highlight);
  noStroke();
  smooth();

  // Create interface components
  String clubName = prefs.getString(0, 1);
  PImage clubLogo = loadImage("images/logo.png");
  String panelTitle = dayName() + " " + clubName;
  controlPanel = new ControlPanel(panelTitle, clubLogo, endHour, endMin, nTeams, nSim, halfIceThresh, thirdIceThresh);
  sessionBar = new SessionBar();
  matchList = new MatchList(nSim, matchLength);

  // Set window properties
  ImageIcon titlebaricon = new ImageIcon(loadBytes("images/icon_16.gif"));
  frame.setIconImage(titlebaricon.getImage());
  frame.setTitle(panelTitle + " | IceTimer 1.2");
}

public void draw() {
  // Update highlight colour if necessary
  if (sessionBar.hasChangedColour) {
    int newColour = sessionBar.getHighlightColour();
    highlight = newColour;
    controlPanel.setHighlightColour(newColour);
    sessionBar.hasChangedColour = false;
  }

  // Texture background
  background(highlight);
  fill(lerpColor(highlight, TIMER_GREY, 0.1f));
  for (int i = 0; i < height; i++) {
    if (PApplet.parseInt(i/3.0f) % 2 == 1) {
      rect(0, i, width, 1);
    }
  }

  // Refresh timers
  matchList.update();
  sessionBar.update();

  // Display interface components
  matchList.display();
  controlPanel.display();
  sessionBar.display();
}

public void keyPressed() {
  if (key == ' ') {
    matchList.playPause();
  } else if (key == '+') {
    matchList.advanceGame(1);
  } else if (key == '_') {
    matchList.advanceGame(-1);
  } else if (key == 'C') {
    matchList.toggleMiniButtons();
  } else if (key == 'F') {
    toggleFullScreen();
  }
}

public void mousePressed() {
  if (mouseY <= 150) {
    // Get control panel to respond to mouse press
    controlPanel.respond(mouseX, mouseY);

    // Start or stop session if necessary
    boolean newInSession = !controlPanel.isActive;
    if (inSession != newInSession) {
      inSession = newInSession;
      if (inSession) {
        startSession();
      } else {
        endSession();
      }
    }
  } else {
    // Get match list to respond to mouse press 
    matchList.respond(mouseX, mouseY);
  }
}

public String dayName() {
  // Returns full name of week day
  Calendar c = Calendar.getInstance();
  int day = c.get(Calendar.DAY_OF_WEEK);
  String names[] = {
    "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
  };
  return names[day-1];
}

public int dayNumber() {
  // Returns weekday index, 1 for Sunday through 7 for Saturday
  Calendar c = Calendar.getInstance();
  int weekday = c.get(Calendar.DAY_OF_WEEK);
  return weekday;
}

public void endSession() {
  // Trigger session end
  sessionBar.deactivate();
  matchList.deactivate();
}

public void startSession() {
  // Get final parameters from control panel
  int endHour = controlPanel.getEndHour();
  int endMin = controlPanel.getEndMin();
  int nTeams = controlPanel.getNTeams();
  int nSim = controlPanel.getNSim();

  // Initiate session
  sessionBar.activate(endHour, endMin);
  matchList.activate(nSim);
  matchList.reloadMatches(nTeams, nSim);
}

public boolean sketchFullScreen() {
  // Decide whether to enter full screen using prefs.csv
  Table prefs = loadTable("prefs.csv");
  boolean fullScreen = PApplet.parseBoolean(prefs.getString(7, 1));
  return fullScreen;
}

public void toggleFullScreen() {
  // Changes the saved preference for full screen or windowed mode
  fullScreen = !fullScreen;
  String[] prefStrings = loadStrings("prefs.csv");
  prefStrings[7] = "fullScreen," + str(fullScreen);

  // Use first path for OSX, second for Windows
  //saveStrings("IceTimer.app/Contents/Java/data/prefs.csv", prefStrings);
  saveStrings("data/prefs.csv", prefStrings);

  // Show that the change has been made
  String newTitle = fullScreen ? "Full screen on restart" : "Windowed on restart";
  controlPanel.setTitle(newTitle);
}

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

class Button {
  int xCentre, yCentre;
  boolean isActive, wasPressed;
  int activeColour, inactiveColour;
  String label;
  PFont buttonFont;

  Button(int xCentre_, int yCentre_, String label_, int activeColour_, int inactiveColour_) {
    xCentre = xCentre_;
    yCentre = yCentre_;
    label = label_;
    activeColour = activeColour_;
    inactiveColour = inactiveColour_;

    isActive = true;
    wasPressed = false;
    buttonFont = loadFont("fonts/SquarishSansCTRegular-24.vlw");
  }

  public void display() {
    // Draw label
    textAlign(CENTER);
    textFont(buttonFont);
    if (isActive) {
      fill(activeColour);
    } else {
      fill(inactiveColour);
    }
    text(label, xCentre, yCentre);

    // Draw border
    noFill();
    strokeWeight(2);
    if (isActive && mouseX >= xCentre-60 && mouseX <= xCentre+60 && mouseY >= yCentre-20 && mouseY <= yCentre+12) {
      stroke(activeColour);
    } else {
      stroke(inactiveColour);
    }
    rect(xCentre-60, yCentre-23, 120, 32);
    noStroke();
  }

  public void respond(int clickX, int clickY) {
    if (isActive && clickX >= xCentre-60 && clickX <= xCentre+60 && clickY >= yCentre-20 && clickY <= yCentre+12) {
      wasPressed = true;
    }
  }

  public void setActiveColour(int newColour) {
    activeColour = newColour;
  }
}

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

  public void display() {
    // Fill cell
    for (int i = 0; i < ySize; i++) {
      float fillColour = map(i, 0, ySize, (isActive ? 200 : 60), (isActive ? 180 : 50));
      fill(fillColour);
      rect(xPos, yPos+i, xSize, 1);
    }

    // Draw outline and dividing lines
    noFill();
    stroke(0);
    strokeWeight(1.5f);
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
      if (floor(leftTeam/10.0f) == 1) {
        xCentre -= 8;
      }
      if (rightTeam%10 == 1) {
        xCentre += 8;
      }
      if (leftTeam < 10 && floor(rightTeam/10.0f) == 1) { 
        xCentre -= 20;
      } else if (leftTeam < 10 && floor(rightTeam/10.0f) > 1) { 
        xCentre -= 25;
      } 
      if (floor(leftTeam/10.0f) == 1 && floor(rightTeam/10.0f) == 1) {
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
      text(rightTeam, xCentre+(floor(rightTeam/10.0f) == 1 ? 14 : 25), yPos+70);
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

  public void respond(int clickX, int clickY) {
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

  public void setXPos(int newPos) {
    xPos = newPos;
    updateButtons();
  }

  public void setYPos(int newPos) { 
    yPos = newPos;
    updateButtons();
  }

  public void setFixture(TableRow newFixture) {
    fixture = newFixture;
    nMatches = fixture.getColumnCount()/2;
  }

  public void setGameNo(int newNo) {
    gameNo = newNo;
  }

  public void setHeight(int newSize) {
    ySize = newSize;
    updateButtons();
  }

  public void setMinLeft(int newMin) {
    minLeft = newMin;
  }

  public void setSecLeft(int newSec) {
    secLeft = newSec;
  }

  public void setWidth(int newSize) {
    xSize = newSize;
    updateButtons();
  }

  public void updateButtons() {
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

class ControlPanel {
  final int FULL_ICE = 0;
  final int HALF_ICE = 1;
  final int THIRD_ICE = 2;

  int halfIceThresh, thirdIceThresh;
  boolean isActive;
  String title;
  int highlightColour;
  PImage logo;
  PFont panelFont;

  Spinner hourSpinner, minSpinner, teamSpinner;
  Radio simRadio;
  Button startButton, endButton;

  ControlPanel(String title_, PImage logo_, int endHour_, int endMin_, int nTeams_, int nSim_, int halfIceThresh_, int thirdIceThresh_) {
    title = title_;   
    logo = logo_;

    isActive = true;
    highlightColour = TIMER_GREEN;
    panelFont = loadFont("fonts/SquarishSansCTRegular-24.vlw");

    // Store the values of non-editable properties
    // These can be set in prefs.csv
    halfIceThresh = halfIceThresh_;
    thirdIceThresh = thirdIceThresh_;

    String[] labels = {
      "Full ice", "Half ice", "Third ice"
    };

    // Create widgets
    // Padding offset is used to cope with different screen sizes
    int xOffset = max(0, width/2-571);
    hourSpinner = new Spinner(486+xOffset, 82, endHour_, 0, 23, 1, highlightColour, TIMER_GREY);
    minSpinner = new Spinner(556+xOffset, 82, endMin_, 0, 55, 5, highlightColour, TIMER_GREY);
    teamSpinner = new Spinner(486+xOffset, 110, nTeams_, 3, 30, 1, highlightColour, TIMER_GREY);
    simRadio = new Radio(657+xOffset, 53, labels, nSim_-1, highlightColour, TIMER_GREY);
    startButton = new Button(width-1000 >= 40 ? width-116 : width-106, 62, "START", highlightColour, TIMER_GREY);
    endButton = new Button(width-1000 >= 40 ? width-116 : width-106, 104, "END", highlightColour, TIMER_GREY);

    // Disable 'END' button
    endButton.isActive = false;
  }

  public void activate() {
    // Enable widgets
    hourSpinner.isActive = true;
    minSpinner.isActive = true;
    teamSpinner.isActive = true;
    simRadio.isActive = true;
    startButton.isActive = true;

    // Disable 'END' button
    endButton.isActive = false;

    // Flag change
    isActive = true;
  }

  public void adjustSimRadio() {
    // Makes sure that only allowed options are selectable

    // Get current values
    int currentIce = simRadio.getSelectedIndex();
    int newNTeams = teamSpinner.getValue();

    // Set default option
    simRadio.enableAll();
    if (newNTeams < halfIceThresh) {
      simRadio.setSelected(FULL_ICE);
    } else if (newNTeams >= halfIceThresh && newNTeams < thirdIceThresh) {
      simRadio.setSelected(HALF_ICE);
    } else {
      simRadio.setSelected(THIRD_ICE);
    }

    // Disable options with unavailable fixtures
    if (newNTeams > 12) {
      simRadio.disableOption(FULL_ICE);
    }
    if (newNTeams < 5 || newNTeams > 17) {
      simRadio.disableOption(HALF_ICE);
    }
    if (newNTeams < 8) {
      simRadio.disableOption(THIRD_ICE);
    }
  }

  public void deactivate() {
    // Disable widgets
    hourSpinner.isActive = false;
    minSpinner.isActive = false;
    teamSpinner.isActive = false;
    simRadio.isActive = false;
    startButton.isActive = false;

    // Enable 'END' button
    endButton.isActive = true;

    // Flag change
    isActive = false;
  }

  public void display() {
    // Introduce variable padding to cope with different screen sizes
    int xOffset = max(0, width/2-571);

    // Draw shadow
    for (int i = 0; i < 15; i++) {
      fill(0, 0, 0, 90-6*i);
      rect(0, 150+i, width, 1);
    }

    // Draw panel
    fill(0);
    rect(0, 0, width, 150);

    // Draw logo
    tint(highlightColour);
    image(logo, 56, 28, 240, 240*(111.0f/300.0f));

    // Print title
    textFont(panelFont);
    textAlign(LEFT);
    fill(TIMER_GREY);
    text(title, 356+xOffset, 53);

    // Print labels
    if (isActive) {
      fill(highlightColour);
    } else {
      fill(TIMER_GREY);
    }
    text("End time", 356+xOffset, 83);
    text(":", 542+xOffset, 82);
    text("Teams", 356+xOffset, 111);

    // Display widgets
    hourSpinner.display();
    minSpinner.display();
    teamSpinner.display();
    simRadio.display();
    startButton.display();
    endButton.display();
  }

  public int getEndHour() {
    return hourSpinner.getValue();
  }

  public int getEndMin() {
    return minSpinner.getValue();
  }

  public int getNSim() {
    int n = 1 + simRadio.getSelectedIndex();
    return n;
  }

  public int getNTeams() {
    return teamSpinner.getValue();
  }

  public void respond(int clickX, int clickY) {
    // Delegate click to widgets
    hourSpinner.respond(clickX, clickY);
    minSpinner.respond(clickX, clickY);
    teamSpinner.respond(clickX, clickY);
    simRadio.respond(clickX, clickY);
    startButton.respond(clickX, clickY);
    endButton.respond(clickX, clickY);

    // Adjust ice options given number of teams
    if (teamSpinner.changeMade) {
      adjustSimRadio();
      teamSpinner.changeMade = false;
    }

    // Pass on start or end of session
    if (startButton.wasPressed) {
      startButton.wasPressed = false;
      deactivate();
    } else if (endButton.wasPressed) {
      endButton.wasPressed = false;
      activate();
    }
  }

  public void setHighlightColour(int newColour) {
    highlightColour = newColour;

    // Set widget colours to match
    hourSpinner.setActiveColour(newColour);
    minSpinner.setActiveColour(newColour);
    teamSpinner.setActiveColour(newColour);
    simRadio.setActiveColour(newColour);
    startButton.setActiveColour(newColour);
    endButton.setActiveColour(newColour);
  }

  public void setTitle(String newTitle) {
    title = newTitle;
  }
}

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

class IconButton {
  // Button which displays an icon instead of a string label.
  // Icon should be a 48x48 pixel white/grayscale mask.

  int xPos, yPos, xSize, ySize;
  boolean isActive, wasPressed, displayLabel;
  int activeColour, inactiveColour;
  PImage icon;

  IconButton(int xPos_, int yPos_, int xSize_, int ySize_, PImage icon_, int activeColour_, int inactiveColour_) {
    xPos = xPos_;
    yPos = yPos_;
    xSize = xSize_;
    ySize = ySize_;
    icon = icon_;
    activeColour = activeColour_;
    inactiveColour = inactiveColour_;

    isActive = true;
    wasPressed = false;
  }

  public void display() {
    // Highlight background when hovered over
    if (isActive && mouseX >= xPos && mouseX <= xPos+xSize && mouseY >= yPos && mouseY <= yPos+ySize) {
      //fill(activeColour, 150);
      fill(TIMER_AMBER, 200);
      rect(xPos, yPos, xSize, ySize);
    }

    // Draw icon
    tint(inactiveColour);
    image(icon, xPos+xSize/2-23, yPos+ySize/2-23, 48, 48);

    // Draw border
    noFill();
    strokeWeight(1.7f);
    stroke(inactiveColour);
    rect(xPos, yPos, xSize, ySize);
    noStroke();
  }

  public void resizeTo(int newXPos, int newYPos, int newXSize, int newYSize) {
    xPos = newXPos;
    yPos = newYPos;
    xSize = newXSize;
    ySize = newYSize;
  }

  public void respond(int clickX, int clickY) {
    if (isActive && clickX >= xPos && clickX <= xPos+xSize && clickY >= yPos && clickY <= yPos+ySize) {
      wasPressed = true;
    }
  }

  public void setActiveColour(int newColour) {
    // Currently has no effect - see display()
    activeColour = newColour;
  }

  public void setIcon(PImage newIcon) {
    icon = newIcon;
  }
}

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
    listLength = ceil((height-190.0f)/CELL_HEIGHT) + 1;
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

  public void activate(int nSimGames_) {
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


  public void advanceGame(int step) {
    inGame = false;

    // Reset selected cell
    cells[1].isTicking = false;
    cells[1].updateButtons();
    cells[1].setMinLeft(floor(matchLength/60));
    cells[1].setSecLeft(matchLength % 60);

    // Reset timing data
    millisRemaining = matchLength * 1000;
    millisElapsed = 0;
    progress = 0.0f;

    // Realign cells with 'now' line
    offsetCells(progress);

    currentGame += step;
    if (currentGame < 0) {
      currentGame += 100;
    }
    populateCells();
  }

  public void display() {
    // Draw 'now' line
    if (isActive) {
      // Blink line in last 10 seconds
      int secLeft = floor(millisRemaining/1000.0f);
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

  public void deactivate() {
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

  public void offsetCells(float percentage) {
    // Scrolls cells upwards as game progresses
    for (int i = 0; i < listLength; i++) {
      int offsetYPos = round(yStart+(i-1)*cellHeight-percentage*cellHeight);
      cells[i].setYPos(offsetYPos);
    }
  }

  public void pauseGame() {
    inGame = false;
    cells[1].isTicking = false;
    cells[1].updateButtons();
  }
  
  public Table permuteTableRows(Table inTable, int nSimGames) {
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
      int offset = 2*PApplet.parseInt(random(nSimGames));
      for (int j = 0; j < nSimGames*2; j++) {
        outTable.setInt(i, j, inTable.getInt(i, (j+offset)%(nSimGames*2)));
      }
    }
    return outTable;
  }

  public void playPause() {
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

  public void populateCells() {
    // Fills cells with match data
    for (int i = 0; i < listLength; i++) {
      // Loop indeces round
      int idx = (i+currentGame-2)%50 < 0 ? (i+currentGame-2)%50+50 : (i+currentGame-2)%50;
      cells[i].setFixture(matches.getRow(idx));
      cells[i].setGameNo(i+currentGame-1);
    }
  }

  public void reloadMatches(int numTeams, int numSimGames) {
    // Loads new match list from file and repopulates cells
    String fixtureFile = "nsim-nteams/" + numSimGames + "-" + numTeams + ".csv";
    Table tempTable = loadTable(fixtureFile);
    matches = permuteTableRows(tempTable, numSimGames);
    populateCells();
  }

  public void respond(int clickX, int clickY) {
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

  public void startGame() {
    inGame = true;
    cells[1].isTicking = true;
    cells[1].updateButtons();

    if (millisElapsed > 0) {
      // Resume current game
      startMillis = millis() - millisElapsed;
    } else {
      // Begin new game
      startMillis = millis();
      progress = 0.0f;
      millisElapsed = 0;
      millisRemaining = matchLength * 1000;
    }
  }

  public void toggleMiniButtons() {
    // Toggles between wide and mini (square) match buttons
    boolean newSetting = !(cells[0].miniButtons);
    for (int i = 0; i < listLength; i++) {
      cells[i].miniButtons = newSetting;
      cells[i].updateButtons();
    }
  }

  public void update() {
    // Update match timer during game
    if (isActive && inGame && millisRemaining > 0) {
      millisElapsed = millis() - startMillis;
      progress = (1.0f * millisElapsed) / (matchLength * 1000.0f);

      // Calculate remaining play time
      millisRemaining = (matchLength * 1000) - millisElapsed;
      if (millisRemaining < 0) {
        millisRemaining = 0;
      }

      // Update clock display
      int minRemaining = floor(millisRemaining/(60.0f*1000.0f));
      int secRemaining = floor(millisRemaining/1000.0f) % 60;
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

class Radio {
  final int DEACTIVATED = 0;
  final int DESELECTED = 1;
  final int SELECTED = 2;

  int xPos, yPos, value, nOptions;
  int[] states;
  String[] options;
  boolean isActive, changeMade;
  int activeColour, inactiveColour;
  PFont radioFont;

  Radio(int xPos_, int yPos_, String[] options_, int startValIndex, int activeColour_, int inactiveColour_) {
    xPos = xPos_;
    yPos = yPos_;

    options = options_;
    nOptions = options.length;

    // Set states of options given initial value
    value = startValIndex;
    states = new int[nOptions];
    for (int i = 0; i < nOptions; i++) {
      if (i == value) {
        states[i] = SELECTED;
      } else {
        states[i] = DESELECTED;
      }
    }

    activeColour = activeColour_;
    inactiveColour = inactiveColour_;

    isActive = true;
    changeMade = false;

    radioFont = loadFont("fonts/SquarishSansCTRegular-24.vlw");
  }

  public void disableOption(int index) {
    // Doesn't deal with option if it is currently selected
    // To disable a selected option, first change the selection
    if (states[index] == DESELECTED) {
      states[index] = DEACTIVATED;
    }
  }

  public void display() {
    textAlign(LEFT);
    textFont(radioFont);
    for (int i = 0; i < nOptions; i++) {
      // Draw text labels
      if (states[i] == DEACTIVATED || !isActive) {
        fill(inactiveColour);
      } else {
        fill(activeColour);
      }
      text(options[i], xPos+19, yPos + i*29);

      // Draw option circles
      noFill();
      strokeWeight(2);
      if (isActive && states[i] != DEACTIVATED && mouseX >= xPos-12 && mouseX <= xPos+144 && mouseY >= (yPos-18+i*29) && mouseY < (yPos+11+i*29)) {
        stroke(activeColour);
      } else {
        stroke(inactiveColour);
      }
      ellipse(xPos, yPos-7 + i*29, 14, 14);

      // Fill and highlight selected circle     
      if (states[i] == SELECTED) {
        if (isActive) {
          fill(activeColour);
          stroke(activeColour);
        } else {
          fill(inactiveColour);
          stroke(inactiveColour);
        }
        ellipse(xPos, yPos-7 + i*29, 14, 14);
      }
      noStroke();
    }
  }

  public void enableAll() {
    // Leaves selected option unchanged
    for (int i = 0; i < nOptions; i++) {
      if (states[i] == DEACTIVATED) {
        states[i] = DESELECTED;
      }
    }
  }

  public void enableOption(int index) {
    // Does not automatically select the option
    if (states[index] == DEACTIVATED) {
      states[index] = DESELECTED;
    }
  }

  public int getSelectedIndex() {
    return value;
  }

  public void respond(int clickX, int clickY) {
    for (int i = 0; i < nOptions; i++) {
      if (isActive && states[i] != DEACTIVATED && clickX >= xPos-12 && clickX <= xPos+144 && clickY >= (yPos-18+i*29) && clickY < (yPos+11+i*29)) {
        // If a selectable option is clicked, select it
        setSelected(i);
      }
    }
  }

  public void setActiveColour(int newColour) {
    activeColour = newColour;
  }

  public void setSelected(int newValueIndex) {
    //Act only if new option can be selected
    if (states[newValueIndex] == DESELECTED) {
      // Deselect old option
      states[value] = DESELECTED;

      //Select new option
      value = newValueIndex;
      states[value] = SELECTED;
      changeMade = true;
    }
  }
}

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

class SessionBar {
  int startMillis, totalMillis, millisElapsed, millisRemaining, hoursElapsed, minsElapsed, hoursRemaining, minsRemaining;
  boolean isActive, hasChangedColour;
  int highlightColour;
  float progress;
  String timeElapsed, timeRemaining;
  PFont barFont;

  SessionBar() {
    // Load font
    barFont = loadFont("fonts/SquarishSansCTRegular-24.vlw");

    // Initialise variables
    timeElapsed = String.format("%02d" + ":" + "%02d", 0, 0);
    timeRemaining = String.format("%02d" + ":" + "%02d", 0, 0);
    isActive = false;
    hasChangedColour = false;
  }

  public void activate(int endHour, int endMin) {
    // Calculate time remaining until inputted end time
    int hours = endHour - hour();
    if (hours < 0) {
      hours += 24;
    }
    int mins = endMin - minute();
    int secs = -1 * second();
    totalMillis = 1000 * (secs + 60 * (mins + 60 * hours));
    startMillis = millis();
    millisElapsed = 0;
    millisRemaining = totalMillis;
    progress = 0.0f;
    isActive = true;
  }

  public void deactivate() {
    millisRemaining = 0;
    isActive = false;
    hasChangedColour = false;
  }

  public void display() {
    if (isActive) {
      // Set highlight according to time remaining
      // Boolean flags change so that rest of UI will update
      if (millisRemaining > 1000*60*15) {
        // Green if more than 15 mins
        if (highlightColour != TIMER_GREEN) {
          highlightColour = TIMER_GREEN;
          hasChangedColour = true;
        }
      } else if (millisRemaining > 1000*60*5) {
        // Amber for 5 to 25 mins
        if (highlightColour != TIMER_AMBER) {
          highlightColour = TIMER_AMBER;
          hasChangedColour = true;
        }
      } else {
        if (highlightColour != TIMER_RED) {
          // Red for less than 5 mins
          highlightColour = TIMER_RED;
          hasChangedColour = true;
        }
      }
    } else {
      highlightColour = TIMER_GREY;
    }

    // Generate clock string
    hoursRemaining = floor(millisRemaining / (1000.0f*60.0f*60.0f));
    minsRemaining = ceil((millisRemaining - hoursRemaining*1000.0f*60.0f*60.0f) / (1000.0f*60.0f));
    timeRemaining = String.format("%02d" + ":" + "%02d", hoursRemaining, minsRemaining);

    // Draw panel
    fill(0);
    rect(0, height-40, width, 40);

    // Draw shadow
    for (int i = 0; i < 15; i++) {
      fill(0, 0, 0, 90-6*i);
      rect(0, height-(40+i), width, 1);
    }

    // Print session clock
    fill(highlightColour);
    textFont(barFont);
    textAlign(LEFT);
    text("Session", 10, height-13);
    textAlign(RIGHT);
    text(timeRemaining, width-11, height-13);

    // Draw progress bar
    fill(highlightColour);
    rect(121, height-20, width-223, 2);
    fill(TIMER_GREY);
    rect(121, height-20, (width-223.0f)*progress, 2);
  }

  public int getHighlightColour() {
    return highlightColour;
  }

  public void update() {
    // Update session timer during session
    if (isActive && millisRemaining > 0) {
      millisElapsed = millis() - startMillis;
      progress = (1.0f*millisElapsed) / (1.0f*totalMillis);
      millisRemaining = totalMillis - millisElapsed;
    } else if (isActive) {
      deactivate();
    }
  }
}

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

class Spinner {
  int xPos, yPos, value, minVal, maxVal, step;
  boolean isActive, changeMade;
  int activeColour, inactiveColour;
  PFont spinnerFont;

  Spinner(int xPos_, int yPos_, int startVal, int minVal_, int maxVal_, int step_, int activeColour_, int inactiveColour_) {
    xPos = xPos_;
    yPos = yPos_;
    value = startVal;
    minVal = minVal_;
    maxVal = maxVal_;
    step = step_;
    activeColour = activeColour_;
    inactiveColour = inactiveColour_;

    isActive = true;
    changeMade = false;

    spinnerFont = loadFont("fonts/SquarishSansCTRegular-24.vlw");
  }

  public void decrementValue() {
    // Decrement and constrain
    value -= step;
    if (value > maxVal) {
      value = minVal;
    }
    if (value < minVal) {
      value = maxVal;
    }

    // Flag change
    changeMade = true;
  }

  public void display() {
    // Print current value
    if (isActive) {
      fill(activeColour);
    } else {
      fill(inactiveColour);
    }
    textAlign(LEFT);
    textFont(spinnerFont);
    text(String.format("%02d", value), xPos, yPos);

    // Draw boxes
    noFill();
    strokeWeight(2);
    stroke(inactiveColour);
    rect(xPos-2, yPos-17, 53, 20);
    line(xPos+37, yPos-16, xPos+37, yPos+2);
    line(xPos+38, yPos-7, xPos+50, yPos-7);

    // Draw up arrow (highlight on mouseover)
    if (isActive && mouseX >= xPos+37 && mouseX <= xPos+50 && mouseY >= yPos-15 && mouseY <= yPos-4) {
      stroke(activeColour);
    } else {
      stroke(inactiveColour);
    }
    line(xPos+40, yPos-10, xPos+44, yPos-14);
    line(xPos+44, yPos-14, xPos+48, yPos-10);

    // Draw down arrow
    if (isActive && mouseX >= xPos+37 && mouseX <= xPos+50 && mouseY > yPos-4 && mouseY <= yPos+7) {
      stroke(activeColour);
    } else {
      stroke(inactiveColour);
    }
    line(xPos+40, yPos-4, xPos+44, yPos);
    line(xPos+44, yPos, xPos+48, yPos-4);
    noStroke();
  }

  public int getValue() {
    return value;
  }

  public void incrementValue() {
    // Increment and constrain
    value += step;
    if (value > maxVal) {
      value = minVal;
    }
    if (value < minVal) {
      value = maxVal;
    }

    // Flag change
    changeMade = true;
  }


  public void respond(int clickX, int clickY) {
    // Increment or decrement value if arrow is clicked
    if (isActive && clickX >= xPos+37 && clickX <= xPos+50 && clickY >= yPos-15 && clickY <= yPos-4) {
      incrementValue();
    } else if (isActive && clickX >= xPos+37 && clickX <= xPos+50 && clickY > yPos-4 && clickY <= yPos+7) {
      decrementValue();
    }
  }

  public void setActiveColour(int newColour) {
    activeColour = newColour;
  }

  public void setValue(int newValue) {
    // Set and constrain
    value = newValue;
    if (value > maxVal) {
      value = maxVal;
    }
    if (value < minVal) {
      value = minVal;
    }

    // Flag change
    changeMade = true;
  }
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "IceTimer" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
