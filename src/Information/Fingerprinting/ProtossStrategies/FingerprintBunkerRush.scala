package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Generic._
import Utilities.UnitFilters.{IsAll, IsProxied}
import ProxyBwapi.Races.Terran
import Utilities.Time.GameTime

class FingerprintBunkerRush extends FingerprintCompleteBy(
  IsAll(Terran.Bunker, IsProxied),
  GameTime(5,  0)) {
  
  override val sticky = true
}