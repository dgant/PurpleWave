package Planning.Plans.Predicates.Milestones

import Lifecycle.With
import Planning.Plan

class EnemyWalledIn extends Plan {
  
  override def isComplete: Boolean = With.geography.enemyZones.exists(_.walledIn)
  
}
