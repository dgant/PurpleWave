package Planning.Plans.Macro.Milestones

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.UnitClass.UnitClass

class EnemyHasShown(unitClass: UnitClass, quantity: Int = 1) extends Plan {
  
  description.set("Enemy has shown " + quantity + " " + unitClass)
  
  override def isComplete: Boolean = {
    val shown   = With.enemies.map(With.intelligence.unitsShown(_, unitClass)).sum
    val output  = shown > quantity
    output
  }
}
