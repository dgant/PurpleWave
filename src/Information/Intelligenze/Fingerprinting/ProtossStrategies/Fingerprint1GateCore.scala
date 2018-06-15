package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import ProxyBwapi.Races.Protoss

class Fingerprint1GateCore extends FingerprintOr(
  new FingerprintCompleteBy(Protoss.CyberneticsCore,  GameTime(4, 0)),
  new FingerprintCompleteBy(Protoss.Dragoon,          GameTime(4, 32))) {
  
  override val sticky = true
}
