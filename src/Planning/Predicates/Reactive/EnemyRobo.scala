package Planning.Predicates.Reactive

import Planning.Predicates.{MacroFacts, Predicate}

case class EnemyRobo() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyRobo
}