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
  color activeColour, inactiveColour;
  PImage icon;

  IconButton(int xPos_, int yPos_, int xSize_, int ySize_, PImage icon_, color activeColour_, color inactiveColour_) {
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

  void display() {
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
    strokeWeight(1.7);
    stroke(inactiveColour);
    rect(xPos, yPos, xSize, ySize);
    noStroke();
  }

  void resizeTo(int newXPos, int newYPos, int newXSize, int newYSize) {
    xPos = newXPos;
    yPos = newYPos;
    xSize = newXSize;
    ySize = newYSize;
  }

  void respond(int clickX, int clickY) {
    if (isActive && clickX >= xPos && clickX <= xPos+xSize && clickY >= yPos && clickY <= yPos+ySize) {
      wasPressed = true;
    }
  }

  void setActiveColour(color newColour) {
    // Currently has no effect - see display()
    activeColour = newColour;
  }

  void setIcon(PImage newIcon) {
    icon = newIcon;
  }
}

