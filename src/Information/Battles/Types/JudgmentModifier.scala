package Information.Battles.Types

import Debugging.Visualizations.Colors
import bwapi.Color

case class JudgmentModifier(
  var name                  : String = "Modifier",
  var color                 : Color = Colors.DarkGray,
  gainedValueMultiplier     : Double = 0,
  speedMultiplier           : Double = 0,
  targetDelta               : Double = 0) {
  override def toString: String = f"$name${format("V", gainedValueMultiplier)}${format("S", speedMultiplier)}${format("T", targetDelta)}"
  private def format(name: String, value: Double): String = {
    if (value == 0) "" else  f" $name: " + "%1.2f".format(value)
  }
}
