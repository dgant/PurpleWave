package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.Predicate
import ProxyBwapi.UnitClasses.UnitClass

class EnemyHasShown(unitClass: UnitClass, quantity: Int = 1) extends Predicate {
  
  override def apply: Boolean = {
    val shown   = With.unitsShown.allEnemies(unitClass)
    val output  = shown >= quantity
    output
  }
}
