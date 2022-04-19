package Planning.Predicates.Reactive

import Planning.Predicates.{MacroFacts, Predicate}

case class EnemyBasesAtMost(value: Int) extends Predicate {
  override def apply: Boolean = MacroFacts.enemyBases <= value
}
