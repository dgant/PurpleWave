package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Protoss

class Fingerprint1GateCore extends FingerprintOr(
  With.fingerprints.fourGateGoon,
  new FingerprintCompleteBy(Protoss.CyberneticsCore,  GameTime(3, 30)),
  new FingerprintCompleteBy(Protoss.Dragoon,          GameTime(4, 2))) {
  
  override val sticky = true
}
