package Planning.Plans.Predicates.Reactive

import Lifecycle.With
import Planning.Predicate
import ProxyBwapi.Races.Protoss

class EnemyCarriers extends Predicate {
  
  description.set("Is the enemy threatening Carriers?")
  
  override def isComplete: Boolean = {
    With.units.enemy.exists(_.is(Protoss.Carrier))      ||
    With.units.enemy.exists(_.is(Protoss.Interceptor))  ||
    With.units.enemy.exists(_.is(Protoss.FleetBeacon))  ||
    With.units.countEnemy(Protoss.PhotonCannon) > 6
  }
}
