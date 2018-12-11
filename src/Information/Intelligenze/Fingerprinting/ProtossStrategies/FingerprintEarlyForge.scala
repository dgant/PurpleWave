package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import ProxyBwapi.Races.Protoss

class FingerprintEarlyForge extends FingerprintOr(
  new FingerprintCompleteBy(Protoss.Forge,        GameTime(5, 0)),
  new FingerprintCompleteBy(Protoss.PhotonCannon, GameTime(5, 30))) {
  
  override val sticky = true
}
