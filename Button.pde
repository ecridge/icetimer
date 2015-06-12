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

class Button {
  int xCentre, yCentre;
  boolean isActive, wasPressed;
  color activeColour, inactiveColour;
  String label;
  PFont buttonFont;

  Button(int xCentre_, int yCentre_, String label_, color activeColour_, color inactiveColour_) {
    xCentre = xCentre_;
    yCentre = yCentre_;
    label = label_;
    activeColour = activeColour_;
    inactiveColour = inactiveColour_;

    isActive = true;
    wasPressed = false;
    buttonFont = loadFont("fonts/SquarishSansCTRegular-24.vlw");
  }

  void display() {
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

  void respond(int clickX, int clickY) {
    if (isActive && clickX >= xCentre-60 && clickX <= xCentre+60 && clickY >= yCentre-20 && clickY <= yCentre+12) {
      wasPressed = true;
    }
  }

  void setActiveColour(color newColour) {
    activeColour = newColour;
  }
}

