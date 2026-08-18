package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Generic._
import ProxyBwapi.Races.Protoss
import Utilities.Time.GameTime

class FingerprintEarlyForge extends FingerprintOr(
  new FingerprintCompleteBy(Protoss.Forge,        GameTime(3, 0)),
  new FingerprintCompleteBy(Protoss.PhotonCannon, GameTime(4, 0))) {
  
  override val sticky = true
}
