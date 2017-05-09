package Planning.Plans.Macro.Reaction

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.Races.Zerg

class EnemyMutalisks extends Plan {
  
  description.set("Is the enemy threatening Mutalisks?")
  
  override def isComplete: Boolean = {
    With.units.enemy.exists(unit => unit.is(Zerg.Spire) || unit.is(Zerg.Mutalisk))
  }
}
