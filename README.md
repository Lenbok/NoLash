NoLash
======

Standalone command-line tool for GCode software backlash compensation.

Based on RepRap NoLash Software BackLash Compensation Tool written by
Richard White described at
http://forums.reprap.org/read.php?154,178612,182380#msg-182380

Ported to Java by Len Trigg.

https://github.com/Lenbok/NoLash


To Build
--------

You need a Java JDK and ant installed. To build:

$ ant

Which will create an executable jarfile NoLash.jar (which also
contains the src and build.xml)


To Run
------

A Java JRE is sufficient and use it like this:

$ java -jar NoLash.jar 0.15 0.05 <input.gcode >output.gcode

where in this case 0.15 is the amount of X axis backlash to compensate
for, and 0.05 is the amount of Y axis backlash to compensate for.

Alternatively, you can give the name of a gcode file, which will have
the backlash compensation applied to it (the original file is
overwritten), e.g.:

$ java -jar NoLash.jar 0.15 0.05 input.gcode


Automatically calling from your slicer
--------------------------------------

Most slicers have the ability to configure tools to post-process the
gcode and you can hook this tool in there. Copy src/nolash.sh to
somewhere in your PATH and edit the three variables at the top
appropriately. If you're on windows, you'll need to make an equivalent
.BAT script.

For slic3r, go to "Print Settings" then "Output Options", and in the
"Post-processing scripts" section, put in "nolash.sh"

For skeinforge, you can probably do something similar by checking
"Analyze gcode" in the "Export" tab, and then in the "Analyze" tab
checking "Clairvoyance" and putting "nolash.sh" into the "Gcode
Program" field. You'll want to deactivate the built-in backlash
compensation.


To Do
-----

+ Add Windows batch file 

+ Implement Skeinforge backlash compensation method as an alternative
  for comparison of accuracy and impact on print time.

