package Planning.Predicates.Matchup

import Lifecycle.With
import Planning.Predicate
import bwapi.Race

class EnemyIsRace(val race: Race) extends Predicate {
  override def isComplete: Boolean = With.enemies.exists(_.raceCurrent == race)
}