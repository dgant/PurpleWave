package Strategies.UnitMatchers
import bwapi.Unit

object UnitMatchAnything extends UnitMatcher {
  override def accept(unit: Unit): Boolean = true
}
