// RepRap NoLash Software BackLash Compensation Tool
// See http://forums.reprap.org/read.php?154,178612,182380#msg-182380

// Written by Richard White on Jan. 22, 2013
// email georgeflug@gmail.com

// Ported to Java by Len Trigg <lenbok@gmail.com>


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;

public class NoLash {

  // Encapsulates current axis state
  static class Axis {

    final String mName; // "X" or "Y"
    final double mLash; // Amount of lash compensation
    int mDir = 1;    // Current direction of travel
    double mPos = 0; // Current ideal position

    Axis(String name, double lash) {
      mName = name;
      mLash = lash;
    }

    void reset() {
      mDir = 1;
      mPos = 0;
    }
  }

  private final double xLash;
  private final double yLash;

  public NoLash(double x, double y) {
    // divide by 2 to compensate for 2 directions of movement
    // (forward and backward)
    xLash = x / 2;
    yLash = y / 2;
  }

  public void compensate(File gcode) throws IOException {
    File outfile = new File(gcode.getParentFile(), gcode.getName() + "NoLash");
    BufferedReader br = new BufferedReader(new FileReader(gcode));
    try {
      PrintStream ps = new PrintStream(new FileOutputStream(outfile));
      try {
        compensate(br, ps);
        ps.close();
        outfile.renameTo(gcode);
      } finally {
        ps.close();
      }
    } finally {
      br.close(); 
    }
  }

  public void compensate(BufferedReader in, PrintStream out) throws IOException {
    // declare axis state variables
    Axis x = new Axis("X", xLash);
    Axis y = new Axis("Y", yLash);

    // loop through every line
    String line;
    while ((line = in.readLine()) != null) {

      // get the gcode command
      int intIndex = line.indexOf(" ");
      String command;
      if (intIndex == -1) {
        command = line;
      } else {
        command = line.substring(0, line.indexOf(" ")).toUpperCase();
      }

      // update commands if needed
      if ("G0".equals(command) || "G1".equals(command)) {
        line = repairCommand(x, y, line, out);
      } else if ("G28".equals(command)) {
        // save new positions for x and y
        //int intCmd = strCommand.IndexOf("X");
        //int intCmdEnd = strCommand.IndexOf(" ", intCmd);
        //xPos = double.Parse(strCommand.Substring(intCmd + 1, intCmdEnd - intCmd - 1));
        //intCmd = strCommand.IndexOf("Y");
        //intCmdEnd = strCommand.IndexOf(" ", intCmd);
        //xPos = double.Parse(strCommand.Substring(intCmd + 1, intCmdEnd - intCmd - 1));
        x.reset();
        y.reset();
      }
      // write the (potentially adjusted) gcode
      out.println(line);
      
    }
  }


  String mCountermeasure = null;

  private String repairCommand(Axis x, Axis y, String line, PrintStream out) {
    mCountermeasure = "";
    // recalculate x position
    line = repairCommand(x, line);
    // recalculate y position
    line = repairCommand(y, line);
    // print the countermeasure string to the new file
    if (mCountermeasure.length() > 0) {
      out.println(mCountermeasure);
    }
    return line;
  }

  // this routine calculates the new position in the X or Y direction (depending of the axis parameter)
  // axis should be "X" or "Y". Returns updated line and maintains mCountermeasure, updates axis state
  private String repairCommand(Axis axis, String line) {
    
    // get the location of the axis position command
    int intCmd = line.indexOf(axis.mName);
    
    // if the axis does not move during this command then do nothing
    if (intCmd == -1) {
      return line;
    }

    // get the ending location of the position command (denoted by a space or a semicolon)
    int intCmdEnd = indexOfAxisEnd(line, intCmd);

    // parse the position
    double newPos = Double.valueOf(line.substring(intCmd + 1, intCmdEnd));

    // compare to currect position, implement lash countermeasure if necessary
    if (!isSameDirection(axis, newPos) && (axis.mLash > 0)) {
      axis.mDir = -axis.mDir; // reverse the direction of movement
      double calc = axis.mPos + axis.mLash * axis.mDir; // calculate countermeasure position
      // add to 'countermeasure' string
      if (mCountermeasure.length() == 0) {
        mCountermeasure = "G1";
      }
      mCountermeasure += " " + axis.mName + calc;
    }
    axis.mPos = newPos;

    double recalc = newPos + axis.mLash * axis.mDir; // save recalculated position

    // return updated command
    return line.substring(0, intCmd) + axis.mName + recalc + line.substring(intCmdEnd);
  }

  // get the ending position of the axis position quantity (denoted by a space or a semicolon)
  private static int indexOfAxisEnd(String line, int axisStart) {
    int axisEnd = line.indexOf(" ", axisStart);
    if (axisEnd == -1) {
      axisEnd = line.indexOf(";", axisStart);
    } else {
      int commentPos = line.indexOf(";", axisStart);
      if (commentPos > -1) {
        axisEnd = Math.min(axisEnd, commentPos);
      }
    }
    if (axisEnd == -1) {
      axisEnd = line.length();
    }
    return axisEnd;
  }


  // this routine compares the two positions and determines the direction of movement.
  // then it compares the direction of movement to the previous direction of movement.
  // it returns true if the direction is the same, it returns false otherwise
  private boolean isSameDirection(Axis axis, double newPos) {
    int newDir = 0;
    if (axis.mPos < newPos) {
      newDir = 1;
    } else if (axis.mPos > newPos) {
      newDir = -1;
    } else {
      return true;
    }
    return (newDir == axis.mDir);
  }


  public static void main(String... args) throws Exception {
    if (args.length < 2 || args.length > 3) {
      System.err.println("Usage: NoLash XLASH YLASH < input.gcode > output.gcode");
      System.exit(1);
    } else {
      NoLash lash = new NoLash(Double.valueOf(args[0]), Double.valueOf(args[1]));
      if (args.length == 3) {
        lash.compensate(new File(args[2]));
      } else {
        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
          lash.compensate(br, System.out);
        } finally {
          br.close();
        }
      }
    }
  }
}

