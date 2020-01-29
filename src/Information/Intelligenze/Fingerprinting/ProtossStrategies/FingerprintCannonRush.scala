package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchProxied}
import ProxyBwapi.Races.Protoss

class FingerprintCannonRush extends FingerprintCompleteBy(
  UnitMatchAnd(Protoss.PhotonCannon, UnitMatchProxied),
  GameTime(4,  30)) {
  
  override val sticky = true
}