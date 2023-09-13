package Planning.Predicates.Reactive

import Planning.MacroFacts
import Planning.Predicates.Predicate

case class EnemyNaturalConfirmed() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyNaturalConfirmed
}
