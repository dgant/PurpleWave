package Planning.Plans.Macro.Reaction

import Lifecycle.With
import Planning.Plan

class EnemyBasesAtLeast(value: Int) extends Plan {
  
  override def isComplete: Boolean = With.geography.enemyBases.size >= value
  
}
