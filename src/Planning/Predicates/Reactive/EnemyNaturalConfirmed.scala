package Planning.Predicates.Reactive

import Lifecycle.With
import Planning.Predicate

class EnemyNaturalConfirmed extends Predicate {
  override def isComplete: Boolean = With.geography.enemyBases.exists(b => b.isNaturalOf.isDefined && b.townHall.isDefined)
}
