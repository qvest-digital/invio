invio
=====

The given code of our indoor navigation app for LinuxTag 2014 is an addition to our talk "Indoor navigation with Android" held at the LinuxTag 2014 (see http://www.linuxtag.org/2014/de/programm/vortragsdetails/?eventid=1524).

__Note that this is not an executable source code, but it should give you an insight into our implementation.__

App
-----
The purpose of our app is to determine the position of the user within a given map environment and moreover, allowing the user to search for interesting booths and sessions, whose locations are then depicted on the map.

In order to localize the user our app makes use of RSSI measurements from a given WLAN network.
Furthermore, the movement of the user is tracked by utilizing the sensors of the user's mobile device and subsequently, the motions are combined with the wifi measurements by means of a particle filter.
Overall, this leads to the display of smooth movements while providing an adequate accuracy.

Our app considers large locations by facilitating the use of multiple maps.
Besides offering the possibilty of manually switching between maps, our app is able to automatically detect the correct map of the area, in which the user is staying.

The source code of the multimap feature can be found in the directory "invio", whereas "invio-localization" contains the implementation of the localization algorithms as well as an admin app to manage the maps and to change localization settings.
