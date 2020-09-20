package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Generic.{FingerprintCompleteBy, GameTime}
import ProxyBwapi.Races.Protoss

// Sample Core-on-14 Core completion time: 2:45
// Sample ZCore Core completion time: 3:08
class FingerprintCoreBeforeZealot extends FingerprintCompleteBy(Protoss.CyberneticsCore, GameTime(2, 57))
