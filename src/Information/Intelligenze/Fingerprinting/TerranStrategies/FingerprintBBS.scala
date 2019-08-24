package Information.Intelligenze.Fingerprinting.TerranStrategies

import Information.Intelligenze.Fingerprinting.Generic.{FingerprintArrivesBy, FingerprintCompleteBy, FingerprintOr, GameTime}
import ProxyBwapi.Races.Terran

class FingerprintBBS extends FingerprintOr(
  new FingerprintCompleteBy(Terran.Barracks,  GameTime(2, 35), 2),
  new FingerprintCompleteBy(Terran.Marine,    GameTime(2, 40), 2),
  new FingerprintCompleteBy(Terran.Marine,    GameTime(2, 55), 4),
  new FingerprintCompleteBy(Terran.Marine,    GameTime(3, 10), 6),
  new FingerprintCompleteBy(Terran.Marine,    GameTime(3, 25), 8),
  new FingerprintCompleteBy(Terran.Marine,    GameTime(3, 40), 10),
  new FingerprintCompleteBy(Terran.Marine,    GameTime(3, 55), 12),
  new FingerprintArrivesBy(Terran.Marine,     GameTime(3, 10), 2),
  new FingerprintArrivesBy(Terran.Marine,     GameTime(3, 25), 4),
  new FingerprintArrivesBy(Terran.Marine,     GameTime(3, 45), 6),
  new FingerprintArrivesBy(Terran.Marine,     GameTime(5,  0), 8),
  new FingerprintArrivesBy(Terran.Marine,     GameTime(4, 20), 10),
  new FingerprintArrivesBy(Terran.Marine,     GameTime(4, 40), 12))