package Micro.Formation

import Debugging.{SimpleString, ToString}
import Debugging.Visualizations.Colors
import bwapi.Color

abstract class FormationStyle(val color: Color, val offset: Int) extends SimpleString {
  val name: String = ToString(this).replace("FormationStyle", "")
}

object FormationStyleEngage     extends FormationStyle(Colors.BrightGreen,  0)
object FormationStyleMarch      extends FormationStyle(Colors.BrightTeal,   1)
object FormationStyleDisengage  extends FormationStyle(Colors.BrightRed,    2)
object FormationStyleGuard      extends FormationStyle(Colors.NeonBlue,     3)
object FormationStyleEmpty      extends FormationStyle(Colors.MediumGray,   4)

