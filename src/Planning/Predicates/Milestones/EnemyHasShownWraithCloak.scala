package Planning.Predicates.Milestones

import Planning.MacroFacts
import Planning.Predicates.Predicate
import ProxyBwapi.Races.Terran

case class EnemyHasShownWraithCloak() extends Predicate {
  override def apply: Boolean = MacroFacts.enemyHasTech(Terran.WraithCloak)
}
