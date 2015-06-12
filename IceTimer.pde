//    
//    IceTimer - Graphical timer and match scheduler for pick-up ice hockey
//    Copyright (C) 2014–15 Joe Cridge
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
//    Joe Cridge, February 2015.
//    <joe.cridge@me.com>
//

import java.util.Calendar;
import javax.swing.ImageIcon;

final color TIMER_GREY = color(60);
final color TIMER_GREEN = color(0, 175, 0);
final color TIMER_AMBER = color(220, 130, 0);
final color TIMER_RED = color(195, 0, 0);

boolean inSession, fullScreen;
color highlight;

ControlPanel controlPanel;
SessionBar sessionBar;
MatchList matchList;

void setup() {
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
  fullScreen = boolean(prefs.getString(7, 1));

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

void draw() {
  // Update highlight colour if necessary
  if (sessionBar.hasChangedColour) {
    color newColour = sessionBar.getHighlightColour();
    highlight = newColour;
    controlPanel.setHighlightColour(newColour);
    sessionBar.hasChangedColour = false;
  }

  // Texture background
  background(highlight);
  fill(lerpColor(highlight, TIMER_GREY, 0.1));
  for (int i = 0; i < height; i++) {
    if (int(i/3.0) % 2 == 1) {
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

void keyPressed() {
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

void mousePressed() {
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

String dayName() {
  // Returns full name of week day
  Calendar c = Calendar.getInstance();
  int day = c.get(Calendar.DAY_OF_WEEK);
  String names[] = {
    "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
  };
  return names[day-1];
}

int dayNumber() {
  // Returns weekday index, 1 for Sunday through 7 for Saturday
  Calendar c = Calendar.getInstance();
  int weekday = c.get(Calendar.DAY_OF_WEEK);
  return weekday;
}

void endSession() {
  // Trigger session end
  sessionBar.deactivate();
  matchList.deactivate();
}

void startSession() {
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

boolean sketchFullScreen() {
  // Decide whether to enter full screen using prefs.csv
  Table prefs = loadTable("prefs.csv");
  boolean fullScreen = boolean(prefs.getString(7, 1));
  return fullScreen;
}

void toggleFullScreen() {
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

