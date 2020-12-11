package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Generic._
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchProxied}
import ProxyBwapi.Races.Protoss
import Utilities.GameTime

class FingerprintCannonRush extends FingerprintCompleteBy(
  UnitMatchAnd(Protoss.PhotonCannon, UnitMatchProxied),
  GameTime(4,  30)) {
  
  override val sticky = true
}