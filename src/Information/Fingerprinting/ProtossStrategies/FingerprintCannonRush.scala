package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Generic._
import Utilities.UnitFilters.{IsAll, IsProxied}
import ProxyBwapi.Races.Protoss
import Utilities.Time.GameTime

class FingerprintCannonRush extends FingerprintCompleteBy(
  IsAll(Protoss.PhotonCannon, IsProxied),
  GameTime(4,  30)) {
  
  override val sticky = true
}