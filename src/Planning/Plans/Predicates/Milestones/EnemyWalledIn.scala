package Planning.Plans.Predicates.Milestones

import Lifecycle.With
import Planning.Predicate

class EnemyWalledIn extends Predicate {
  
  override def isComplete: Boolean = With.geography.enemyZones.exists(_.walledIn)
  
}
