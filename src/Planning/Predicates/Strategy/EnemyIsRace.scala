package Planning.Predicates.Strategy

import Lifecycle.With
import Planning.Predicate
import bwapi.Race

class EnemyIsRace(val race: Race) extends Predicate {
  override def apply: Boolean = With.enemies.exists(_.raceCurrent == race)
}