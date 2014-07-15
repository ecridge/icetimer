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
  color activeColour, inactiveColour;
  PFont spinnerFont;

  Spinner(int xPos_, int yPos_, int startVal, int minVal_, int maxVal_, int step_, color activeColour_, color inactiveColour_) {
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

  void decrementValue() {
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

  void display() {
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

  int getValue() {
    return value;
  }

  void incrementValue() {
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


  void respond(int clickX, int clickY) {
    // Increment or decrement value if arrow is clicked
    if (isActive && clickX >= xPos+37 && clickX <= xPos+50 && clickY >= yPos-15 && clickY <= yPos-4) {
      incrementValue();
    } else if (isActive && clickX >= xPos+37 && clickX <= xPos+50 && clickY > yPos-4 && clickY <= yPos+7) {
      decrementValue();
    }
  }

  void setActiveColour(color newColour) {
    activeColour = newColour;
  }

  void setValue(int newValue) {
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

