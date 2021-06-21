package Planning.Predicates.Reactive

import Lifecycle.With
import Planning.Predicate
import ProxyBwapi.Races.Protoss

case class EnemyCarriers() extends Predicate {
  override def apply: Boolean = {
    With.units.existsEnemy(Protoss.Carrier)      ||
    With.units.existsEnemy(Protoss.Interceptor)  ||
    With.units.existsEnemy(Protoss.FleetBeacon)  ||
    With.units.countEnemy(Protoss.PhotonCannon) > 6
  }
}
