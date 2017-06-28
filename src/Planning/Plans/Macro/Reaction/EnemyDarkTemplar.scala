package Planning.Plans.Macro.Reaction

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.Races.Protoss

class EnemyDarkTemplar extends Plan {
  
  description.set("Is the enemy threatening Dark Templar?")
  
  override def isComplete: Boolean = {
    With.units.enemy.exists(unit =>
      unit.is(Protoss.DarkTemplar) ||
      unit.is(Protoss.CyberneticsCore) ||
      unit.is(Protoss.TemplarArchives))
  }
}
