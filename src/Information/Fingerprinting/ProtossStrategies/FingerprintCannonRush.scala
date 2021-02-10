package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Generic._
import Planning.UnitMatchers.{MatchAnd, MatchProxied}
import ProxyBwapi.Races.Protoss
import Utilities.GameTime

class FingerprintCannonRush extends FingerprintCompleteBy(
  MatchAnd(Protoss.PhotonCannon, MatchProxied),
  GameTime(4,  30)) {
  
  override val sticky = true
}