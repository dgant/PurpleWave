package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Generic._
import ProxyBwapi.Races.Terran
import Utilities.Time.GameTime

class Fingerprint2PortWraith extends FingerprintOr(
  new FingerprintCompleteBy(Terran.Starport,  GameTime(5, 15), 2),
  new FingerprintCompleteBy(Terran.Wraith,    GameTime(5, 30), 2),
  new FingerprintCompleteBy(Terran.Wraith,    GameTime(6, 10), 4),
  new FingerprintArrivesBy(Terran.Wraith,     GameTime(5, 55), 2),
  new FingerprintArrivesBy(Terran.Wraith,     GameTime(6, 35), 4)) {
  
  override val sticky = true
}
