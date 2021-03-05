package Planning.Predicates.Reactive

import Lifecycle.With
import Planning.Predicate

class EnemyBasesAtMost(value: Int) extends Predicate {
  
  override def apply: Boolean = With.geography.enemyBases.size <= value
  
}
