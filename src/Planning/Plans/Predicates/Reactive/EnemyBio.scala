package Planning.Plans.Predicates.Reactive

import Lifecycle.With
import Planning.Composition.UnitMatchers.UnitMatchSiegeTank
import Planning.Plan
import ProxyBwapi.Races.Terran

class EnemyBio extends Plan {
  
  description.set("Is the enemy threatening Terran Bio?")
  
  override def isComplete: Boolean = {
    val enemyMech = (
        2 * With.units.countEnemy(UnitMatchSiegeTank)
      + 3 * With.units.countEnemy(Terran.Battlecruiser)
      + With.units.countEnemy(Terran.Vulture)
      + With.units.countEnemy(Terran.Goliath)
      + With.units.countEnemy(Terran.Wraith)
    )
    val enemyBio = (
      With.units.countEnemy(Terran.Marine)
      + With.units.countEnemy(Terran.Firebat)
      + With.units.countEnemy(Terran.Medic)
    )
    
    enemyBio > Math.max(8, enemyMech)
  }
}
