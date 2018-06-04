package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import ProxyBwapi.Races.Protoss

class FingerprintNexusFirst extends FingerprintCompleteBy(Protoss.Nexus,  GameTime(3, 20), 2) {
  
  override val sticky = true
}
