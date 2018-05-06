package Planning.Plans.Predicates.Milestones

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.UnitClasses.UnitClass

class EnemyHasShown(unitClass: UnitClass, quantity: Int = 1) extends Plan {
  
  description.set("Enemy has shown " + quantity + " " + unitClass)
  
  override def isComplete: Boolean = {
    val shown   = With.enemies.map(With.intelligence.unitsShown(_, unitClass)).sum
    val output  = shown > quantity
    output
  }
}
