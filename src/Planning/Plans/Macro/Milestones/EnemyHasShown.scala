package Planning.Plans.Macro.Milestones

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.UnitClass.UnitClass

class EnemyHasShown(unitClass: UnitClass) extends Plan {
  
  description.set("Enemy has shown a " + unitClass)
  
  override def isComplete: Boolean = With.intelligence.enemyHasShown(unitClass)
}
