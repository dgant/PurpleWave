package Planning.Plans.Predicates.Reactive

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.Races.Terran

class EnemyBioAllIn extends Plan {
  
  description.set("Is the enemy threatening a Terran Bio all-in?")
  
  override def isComplete: Boolean = {
    (
      With.units.enemy.count(unit => unit.is(Terran.Marine))    > 8   ||
      With.units.enemy.count(unit => unit.is(Terran.Barracks))  > 2   ||
      With.units.enemy.count(unit => unit.is(Terran.Medic))     > 1   ||
      With.units.enemy.count(unit => unit.is(Terran.Firebat))   > 1   ||
      With.enemies.exists(_.hasTech(Terran.Stim))               ||
      With.enemies.exists(_.getUpgradeLevel(Terran.MarineRange) > 0)
    ) &&
    With.units.enemy.count(unit => unit.is(Terran.Vulture))           == 0 &&
    With.units.enemy.count(unit => unit.is(Terran.SiegeTankSieged))   == 0 &&
    With.units.enemy.count(unit => unit.is(Terran.SiegeTankUnsieged)) == 0 &&
    With.units.enemy.count(unit => unit.is(Terran.Factory))           == 0 &&
    With.units.enemy.count(unit => unit.is(Terran.Goliath))           == 0 &&
    With.geography.enemyBases.size < 2
  }
}
