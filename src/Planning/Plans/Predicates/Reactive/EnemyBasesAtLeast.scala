package Planning.Plans.Predicates.Reactive

import Lifecycle.With
import Planning.Predicate

class EnemyBasesAtLeast(value: Int) extends Predicate {
  
  override def isComplete: Boolean = With.geography.enemyBases.size >= value
  
}
