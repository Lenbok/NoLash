#!/bin/bash
# Applies standalone backlash compensation. Wrapper script for slic3r, kisslicer etc
# The first argument given must be the name of the file to postprocess
# The amount of x and y backlash can either be set in this script below, 
# or given as second and third arguments, respectively


FILE="$1"

# Update the next three lines to coresspond to your settings
LASH_JAR=/home/len/sandboxes/NoLash/NoLash.jar  # Where is the jarfile
XLASH=${2:-0.17}   # Amount of x-axis backlash, or taken from the second argument if given
YLASH=${3:-0.02}   # Amount of y-axis backlash, or taken from the third argument if given

if [ -f "$FILE" ]; then
    java -jar $LASH_JAR $XLASH $YLASH "$FILE"
else
    echo "No gcode file supplied"
fi
