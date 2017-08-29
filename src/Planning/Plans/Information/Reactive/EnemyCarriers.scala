package Planning.Plans.Information.Reactive

import Lifecycle.With
import Planning.Plan
import ProxyBwapi.Races.Protoss

class EnemyCarriers extends Plan {
  
  description.set("Is the enemy threatening Carriers?")
  
  override def isComplete: Boolean = {
    With.units.enemy.exists(_.is(Protoss.Carrier))      ||
    With.units.enemy.exists(_.is(Protoss.Interceptor))  ||
    With.units.enemy.exists(_.is(Protoss.FleetBeacon))  ||
    With.units.enemy.count(_.is(Protoss.PhotonCannon))  > 6
  }
}
