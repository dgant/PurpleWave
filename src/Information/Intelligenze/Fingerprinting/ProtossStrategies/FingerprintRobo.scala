package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import ProxyBwapi.Races.Protoss

class FingerprintRobo extends FingerprintOr(
  new FingerprintCompleteBy(Protoss.RoboticsFacility,   GameTime(6, 0)),
  new FingerprintCompleteBy(Protoss.RoboticsSupportBay, GameTime(7, 0)),
  new FingerprintCompleteBy(Protoss.Observatory,        GameTime(7, 0)),
  new FingerprintCompleteBy(Protoss.Reaver,             GameTime(8, 30)),
  new FingerprintCompleteBy(Protoss.Observer,           GameTime(7, 0)),
  new FingerprintCompleteBy(Protoss.Shuttle,            GameTime(7, 30)))
  {
  
  override val sticky = true
}
