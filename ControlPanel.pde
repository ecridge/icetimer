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
  color highlightColour;
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
    hourSpinner = new Spinner(486, 82, endHour_, 0, 23, 1, highlightColour, TIMER_GREY);
    minSpinner = new Spinner(556, 82, endMin_, 0, 55, 5, highlightColour, TIMER_GREY);
    teamSpinner = new Spinner(486, 110, nTeams_, 3, 30, 1, highlightColour, TIMER_GREY);
    simRadio = new Radio(657, 53, labels, nSim_-1, highlightColour, TIMER_GREY);
    startButton = new Button(894, 62, "START", highlightColour, TIMER_GREY);
    endButton = new Button(894, 104, "END", highlightColour, TIMER_GREY);

    // Disable 'END' button
    endButton.isActive = false;
  }

  void activate() {
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

  void adjustSimRadio() {
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

  void deactivate() {
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

  void display() {
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
    image(logo, 56, 28, 240, 240*(111.0/300.0));

    // Print title
    textFont(panelFont);
    textAlign(LEFT);
    fill(TIMER_GREY);
    text(title, 356, 53);

    // Print labels
    if (isActive) {
      fill(highlightColour);
    } else {
      fill(TIMER_GREY);
    }
    text("End time", 356, 83);
    text(":", 542, 82);
    text("Teams", 356, 111);

    // Display widgets
    hourSpinner.display();
    minSpinner.display();
    teamSpinner.display();
    simRadio.display();
    startButton.display();
    endButton.display();
  }

  int getEndHour() {
    return hourSpinner.getValue();
  }

  int getEndMin() {
    return minSpinner.getValue();
  }

  int getNSim() {
    int n = 1 + simRadio.getSelectedIndex();
    return n;
  }

  int getNTeams() {
    return teamSpinner.getValue();
  }

  void respond(int clickX, int clickY) {
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

  void setHighlightColour(color newColour) {
    highlightColour = newColour;

    // Set widget colours to match
    hourSpinner.setActiveColour(newColour);
    minSpinner.setActiveColour(newColour);
    teamSpinner.setActiveColour(newColour);
    simRadio.setActiveColour(newColour);
    startButton.setActiveColour(newColour);
    endButton.setActiveColour(newColour);
  }
}

