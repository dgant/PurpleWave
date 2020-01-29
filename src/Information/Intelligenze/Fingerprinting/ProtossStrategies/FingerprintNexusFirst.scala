package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import ProxyBwapi.Races.Protoss

class FingerprintNexusFirst extends FingerprintCompleteBy(Protoss.Nexus,  GameTime(3, 30), 2) {
  
  override val sticky = true
}
