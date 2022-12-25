package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Generic._
import ProxyBwapi.Races.Protoss
import Utilities.Time.GameTime

class FingerprintRobo extends FingerprintOr(
  new FingerprintCompleteBy(Protoss.RoboticsFacility,   GameTime(6, 0)),
  new FingerprintCompleteBy(Protoss.RoboticsSupportBay, GameTime(7, 0)),
  new FingerprintCompleteBy(Protoss.Observatory,        GameTime(7, 0)),
  new FingerprintCompleteBy(Protoss.Reaver,             GameTime(8, 30)),
  new FingerprintCompleteBy(Protoss.Observer,           GameTime(7, 0)),
  new FingerprintArrivesBy(Protoss.Observer,            GameTime(7, 45)),
  new FingerprintCompleteBy(Protoss.Shuttle,            GameTime(7, 30))) {
  
  override val sticky = true
}
