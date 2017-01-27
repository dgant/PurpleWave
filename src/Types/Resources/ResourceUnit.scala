package Types.Resources

import Types.Matcher.UnitMatcher
import Types.Quantities.Quantity

class ResourceUnit(quantity:Quantity, matcher:UnitMatcher) extends Resource {
  def accept():Boolean = {
    true
  }
  def select():Array[bwapi.Unit] = {
    Array.empty
  }
}