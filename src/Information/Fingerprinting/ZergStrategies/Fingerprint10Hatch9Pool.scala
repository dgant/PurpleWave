package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintCompleteBy}
import Lifecycle.With
import ProxyBwapi.Races.Zerg
import Utilities.GameTime

class Fingerprint10Hatch9Pool extends FingerprintAnd(
  new FingerprintCompleteBy(Zerg.Hatchery, GameTime(2, 40), 2),
  new FingerprintCompleteBy(Zerg.SpawningPool, GameTime(2, 50)) // This timing is about equal to overhatch and early for 10h9p
) {
  override def sticky: Boolean = With.geography.enemyBases.exists(b => b.isNaturalOf.isDefined && b.townHall.isDefined)
}
