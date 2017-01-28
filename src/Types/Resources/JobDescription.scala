package Types.Resources

import Types.Quantities.Quantity
import UnitMatching.Matcher.UnitMatch

class JobDescription(
  val quantity:Quantity,
  val matcher:UnitMatch) {
}
