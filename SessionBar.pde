//    
//    IceTimer - Graphical timer and match scheduler for pick-up ice hockey
//    Copyright (C) 2014 Joe Cridge
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
  color highlightColour;
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

  void activate(int endHour, int endMin) {
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
    progress = 0.0;
    isActive = true;
  }

  void deactivate() {
    millisRemaining = 0;
    isActive = false;
    hasChangedColour = false;
  }

  void display() {
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
    hoursRemaining = floor(millisRemaining / (1000.0*60.0*60.0));
    minsRemaining = ceil((millisRemaining - hoursRemaining*1000.0*60.0*60.0) / (1000.0*60.0));
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
    rect(121, height-20, (width-223.0)*progress, 2);
  }

  color getHighlightColour() {
    return highlightColour;
  }

  void update() {
    // Update session timer during session
    if (isActive && millisRemaining > 0) {
      millisElapsed = millis() - startMillis;
      progress = (1.0*millisElapsed) / (1.0*totalMillis);
      millisRemaining = totalMillis - millisElapsed;
    } else if (isActive) {
      deactivate();
    }
  }
}

