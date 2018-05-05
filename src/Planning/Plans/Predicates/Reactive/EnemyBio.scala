package Planning.Plans.Predicates.Reactive

import Lifecycle.With
import Planning.Composition.UnitMatchers.UnitMatchSiegeTank
import Planning.Plan
import ProxyBwapi.Races.Terran

class EnemyBio extends Plan {
  
  description.set("Is the enemy threatening Terran Bio?")
  
  override def isComplete: Boolean = {
    val enemyMech = (
        With.units.countEnemy(UnitMatchSiegeTank)
      + With.units.countEnemy(Terran.Vulture)
      + With.units.countEnemy(Terran.Goliath)
      + With.units.countEnemy(Terran.Wraith)
      + With.units.countEnemy(Terran.Battlecruiser)
    )
    val enemyBio = (
      With.units.countEnemy(Terran.Marine)
      + With.units.countEnemy(Terran.Firebat)
      + With.units.countEnemy(Terran.Medic)
    )
    
    enemyBio > 2 * enemyMech
  }
}
