package Planning.Plans.Macro.Reaction

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.Races.Terran

class EnemyBio extends Plan {
  
  description.set("Is the enemy threatening Terran Bio?")
  
  override def isComplete: Boolean = {
    With.units.enemy.count(unit => unit.is(Terran.Marine))    > 4   ||
    With.units.enemy.count(unit => unit.is(Terran.Barracks))  > 1   ||
    With.units.enemy.count(unit => unit.is(Terran.Medic))     > 1   ||
    With.units.enemy.count(unit => unit.is(Terran.Firebat))   > 1   ||
    With.enemies.exists(_.hasTech(Terran.Stim))               ||
    With.enemies.exists(_.getUpgradeLevel(Terran.MarineRange) > 0)
  }
}
