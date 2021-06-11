package Micro.Formation

import Debugging.ToString
import Debugging.Visualizations.Colors
import bwapi.Color

class FormationStyle(val color: Color) {
  val name: String = ToString(this).replace("FormationStyle", "")
}

object FormationStyleEmpty extends FormationStyle(Colors.MediumGray)
object FormationStyleMarch extends FormationStyle(Colors.BrightTeal)
object FormationStyleGuard extends FormationStyle(Colors.BrightBlue)
object FormationStyleEngage extends FormationStyle(Colors.BrightGreen)
object FormationStyleDisengage extends FormationStyle(Colors.BrightRed)