package Planning.Plans.Predicates.Reactive

import Lifecycle.With
import Planning.Plan

class EnemyBasesAtMost(value: Int) extends Plan {
  
  override def isComplete: Boolean = With.geography.enemyBases.size <= value
  
}
