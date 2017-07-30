package Planning.Plans.Information.Reactive

import Lifecycle.With
import Planning.Plan

class EnemyBasesAtLeast(value: Int) extends Plan {
  
  override def isComplete: Boolean = With.geography.enemyBases.size >= value
  
}
