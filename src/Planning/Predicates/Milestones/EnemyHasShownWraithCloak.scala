package Planning.Predicates.Milestones

import Planning.Predicates.{MacroFacts, Predicate}
import ProxyBwapi.Races.Terran

case class EnemyHasShownWraithCloak() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyHasTech(Terran.WraithCloak)
}
