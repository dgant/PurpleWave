package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Generic._
import ProxyBwapi.Races.Protoss
import Utilities.Time.GameTime

class FingerprintNexusFirst extends FingerprintCompleteBy(Protoss.Nexus,  GameTime(3, 30), 2) {
  
  override val sticky = true
}
