package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import Utilities.Time.GameTime

class FingerprintGatewayFirst extends FingerprintOr(
  With.fingerprints.oneGateCore,
  With.fingerprints.twoGate,
  With.fingerprints.fourGateGoon,
  new FingerprintCompleteBy(Protoss.Gateway,  GameTime(2, 40)),
  new FingerprintArrivesBy(Protoss.Zealot,    GameTime(3, 45))) {
  
  override val sticky = true
}
