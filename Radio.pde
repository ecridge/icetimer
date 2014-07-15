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
  color activeColour, inactiveColour;
  PFont radioFont;

  Radio(int xPos_, int yPos_, String[] options_, int startValIndex, color activeColour_, color inactiveColour_) {
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

  void disableOption(int index) {
    // Doesn't deal with option if it is currently selected
    // To disable a selected option, first change the selection
    if (states[index] == DESELECTED) {
      states[index] = DEACTIVATED;
    }
  }

  void display() {
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

  void enableAll() {
    // Leaves selected option unchanged
    for (int i = 0; i < nOptions; i++) {
      if (states[i] == DEACTIVATED) {
        states[i] = DESELECTED;
      }
    }
  }

  void enableOption(int index) {
    // Does not automatically select the option
    if (states[index] == DEACTIVATED) {
      states[index] = DESELECTED;
    }
  }

  int getSelectedIndex() {
    return value;
  }

  void respond(int clickX, int clickY) {
    for (int i = 0; i < nOptions; i++) {
      if (isActive && states[i] != DEACTIVATED && clickX >= xPos-12 && clickX <= xPos+144 && clickY >= (yPos-18+i*29) && clickY < (yPos+11+i*29)) {
        // If a selectable option is clicked, select it
        setSelected(i);
      }
    }
  }

  void setActiveColour(color newColour) {
    activeColour = newColour;
  }

  void setSelected(int newValueIndex) {
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

