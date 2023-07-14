package Micro.Formation

import Debugging.{SimpleString, ToString}
import Debugging.Visualizations.Colors
import bwapi.Color

abstract class FormationStyle(val color: Color, val offset: Int) extends SimpleString {
  val name: String = ToString(this).replace("FormationStyle", "")
}

object FormationStylePlug       extends FormationStyle(Colors.White,        0)
object FormationStyleEngage     extends FormationStyle(Colors.BrightGreen,  1)
object FormationStyleMarch      extends FormationStyle(Colors.BrightTeal,   2)
object FormationStyleDisengage  extends FormationStyle(Colors.BrightRed,    3)
object FormationStyleGuard      extends FormationStyle(Colors.NeonBlue,     4)
object FormationStyleEmpty      extends FormationStyle(Colors.MediumGray,   5)

