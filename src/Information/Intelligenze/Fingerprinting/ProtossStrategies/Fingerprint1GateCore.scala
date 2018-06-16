package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import ProxyBwapi.Races.Protoss

class Fingerprint1GateCore extends FingerprintOr(
  new FingerprintCompleteBy(Protoss.CyberneticsCore,  GameTime(3, 45)),
  new FingerprintCompleteBy(Protoss.Dragoon,          GameTime(4, 17))) {
  
  override val sticky = true
}
