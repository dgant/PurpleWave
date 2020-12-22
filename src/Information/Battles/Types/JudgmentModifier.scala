package Information.Battles.Types

import Debugging.Visualizations.Colors
import bwapi.Color

case class JudgmentModifier(
  var name                  : String = "Modifier",
  var color                 : Color = Colors.DarkGray,
  gainedValueMultiplier     : Double = 1,
  speedMultiplier           : Double = 1,
  targetDelta               : Double = 0) {
  override def toString: String = f"$name${format("V", 1, gainedValueMultiplier)}${format("S", 1, speedMultiplier)}${format("T", 0, targetDelta)}"
  private def format(name: String, default: Double, value: Double): String = {
    if (value == default) "" else  f" $name: " + "%1.2f".format(value)
  }
}
