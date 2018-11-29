package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import ProxyBwapi.Races.Protoss

class FingerprintRobo extends FingerprintOr(
  new FingerprintCompleteBy(Protoss.RoboticsFacility,  GameTime(7, 0)),
  new FingerprintCompleteBy(Protoss.RoboticsSupportBay,  GameTime(8, 0)),
  new FingerprintCompleteBy(Protoss.Observatory,  GameTime(8, 0)),
  new FingerprintCompleteBy(Protoss.Reaver,  GameTime(9, 0)),
  new FingerprintCompleteBy(Protoss.Observer,  GameTime(8, 0)),
  new FingerprintCompleteBy(Protoss.Shuttle,  GameTime(8, 0)))
  {
  
  override val sticky = true
}
