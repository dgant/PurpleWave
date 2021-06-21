package Planning.Predicates.Milestones

import Planning.Predicate
import Planning.Predicates.MacroFacts
import ProxyBwapi.Races.Terran

case class EnemyHasShownWraithCloak() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyHasTech(Terran.WraithCloak)
}
