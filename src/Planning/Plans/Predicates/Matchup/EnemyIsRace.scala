package Planning.Plans.Predicates.Matchup

import Lifecycle.With
import Planning.Plan
import bwapi.Race

class EnemyIsRace(val race: Race) extends Plan {
  override def isComplete: Boolean = With.enemies.exists(_.raceCurrent == race)
}