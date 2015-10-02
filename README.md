[![Latest version](https://img.shields.io/badge/latest-v1.2-brightgreen.svg)](https://github.com/joecridge/IceTimer/releases/latest)
[![GitHub license](https://img.shields.io/badge/license-GPLv3-blue.svg)](https://raw.githubusercontent.com/joecridge/IceTimer/master/LICENSE.txt)

[*Version 2.0 is in the works &rarr;*](https://github.com/joecridge/IceTimer2)

IceTimer
========

IceTimer is a graphical timer and match scheduler for round-robin style
informal tournaments. Primarily intended for use in Oxford University
[Alternative Ice Hockey](http://www.oxford-alts.org.uk/) sessions but could
easily be modified for use in other situations.

![IceTimer 1.2](https://cloud.githubusercontent.com/assets/11491479/8140676/b48eda30-1155-11e5-8531-8b88ca420bf7.png)


Features
--------

- Match scheduling for up to 30 teams.
- Up to 3 simultaneous matches.
- Visual indication at 30 and 10 seconds until end of current game.
- Visual indication at 15 and 5 minutes until end of current session.
- Easy to read who's next on the ice.


Installation
------------

Binaries with installation instructions for Windows and Mac OS X are
available from the [releases page](https://github.com/joecridge/IceTimer/releases).


Compilation
-----------

If there is no binary available for your system then you can compile IceTimer
from its source files using the Processing IDE, the latest version of which
can be downloaded from <https://processing.org/download/>.

Make sure that all of the `.pde` files and the data folder are together within
the same parent folder – this should already be the case if you have cloned
the repository or downloaded one of the [archived releases](https://github.com/joecridge/IceTimer/releases).
Use the IDE to open the file `IceTimer.pde` and choose 'Sketch' > 'Run' from
the menu to check that everything is working. You can then export an
application for your system by choosing 'File' > 'Export Application'.

You can also compile IceTimer directly from its Java source file but you will
need to install the Processing library to be able to do this.


Documentation
-------------

Use the controls at the top of the program interface to enter session details
and press 'START' to begin. The background will turn amber to warn when there
are 15 minutes left in the session and red when there are 5 minutes left. You
can end the session by pressing 'END' in order to change the session details
(e.g. if more teams arrive).

The current game is highlighted in the match list and will respond to presses
of the play/pause and skip button controls, the latter of which advances the
current game to the next game in the match list. The highlight will change
colour to indicate when there are 30 seconds and 10 seconds left in the
current game.

When the timer reaches zero, the current game will automatically be advanced
but the clock for the next game will not start until the play button is
pressed; this gives time for the new teams to be read out. You can also use
the spacebar instead of the play/pause button, and Shift-`+` and Shift-`–` to
move freely between games.

The match list will slowly scroll up the screen as games are played. The
horizontal bars indicate the current position in the list and within the
current game, so it should be easy to tell the teams who will be on next from
those who are on now.

You can toggle between full screen and windowed mode by pressing Shift-F while
the program is running; changes take effect when the program is restarted.


Version History
---------------

```
v1.2  + Added Shift-`+` and Shift-`–` shortcuts to move freely between games
      * Match locations within rink are now randomly chosen

v1.1  + Added full screen mode
      * Minor fixes

v1.0  + Initial release
```

Credits
-------

Match fixture lists generated using Jonathan Rennison’s [Match Fixture List
Generator](https://sourceforge.net/projects/matchgen/).
Program typeface is Squarish Sans CT by Tim Larson.
IceTimer is written in Java using [Processing](https://processing.org/).


License
-------

Copyright (C) 2014–2015 Joe Cridge.

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free Software
Foundation, either version 3 of the License, or (at your option) any later
version.

This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE.  
See [LICENSE.txt](https://github.com/joecridge/IceTimer/blob/master/LICENSE.txt).

Joe Cridge, February 2015.  
<mailto:joe.cridge@me.com>
