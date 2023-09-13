package Planning.Predicates.Milestones

import Planning.MacroFacts
import Planning.Predicates.Predicate
import ProxyBwapi.UnitClasses.UnitClass

case class EnemyHasShown(unitClass: UnitClass, quantity: Int = 1) extends Predicate {
  override def apply: Boolean = MacroFacts.enemiesShown(unitClass) >= quantity
}
