package Planning.Predicates.Reactive

import Planning.Predicate
import Planning.Predicates.MacroFacts

case class EnemyNaturalConfirmed() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyNaturalConfirmed
}
