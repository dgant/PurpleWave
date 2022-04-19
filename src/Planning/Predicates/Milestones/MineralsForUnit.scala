package Planning.Predicates.Milestones

import Planning.Predicates.{MacroFacts, Predicate}
import ProxyBwapi.UnitClasses.UnitClass

case class MineralsForUnit(unitClass: UnitClass, quantity: Int = 1) extends Predicate {
  override def apply: Boolean = MacroFacts.haveMineralsForUnit(unitClass, quantity)
}