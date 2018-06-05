package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import Planning.Composition.UnitMatchers.{UnitMatchAnd, UnitMatchProxied}
import ProxyBwapi.Races.Protoss

class FingerprintCannonRush extends FingerprintCompleteBy(
  UnitMatchAnd(Protoss.PhotonCannon, UnitMatchProxied),
  GameTime(3,  0)) {
  
  override val sticky = true
}