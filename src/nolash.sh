#!/bin/bash
# Applies standalone backlash compensation. Wrapper script for slic3r

# Update the next three lines to coresspond to your settings
LASH_JAR=/home/len/sandboxes/NoLash/NoLash.jar  # Where is the jarfile
XLASH=0.17   # Amount of x-axis backlash
YLASH=0.02   # Amount of y-axis backlash

if [ -f "$1" ]; then
    java -jar $LASH_JAR $XLASH $YLASH "$1"
else
    echo "No gcode file supplied"
fi
