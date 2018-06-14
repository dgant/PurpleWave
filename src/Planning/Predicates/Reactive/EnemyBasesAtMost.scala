package Planning.Predicates.Reactive

import Lifecycle.With
import Planning.Predicate

class EnemyBasesAtMost(value: Int) extends Predicate {
  
  override def isComplete: Boolean = With.geography.enemyBases.size <= value
  
}
