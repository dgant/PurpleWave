package Information.Intelligenze.Fingerprinting.TerranStrategies

import Information.Intelligenze.Fingerprinting.Generic.{FingerprintArrivesBy, FingerprintCompleteBy, FingerprintOr, GameTime}
import ProxyBwapi.Races.Terran

class FingerprintBBS extends FingerprintOr(
  new FingerprintCompleteBy(Terran.Barracks,  GameTime(2, 35), 2),
  new FingerprintCompleteBy(Terran.Marine,    GameTime(2, 50), 2),
  new FingerprintCompleteBy(Terran.Marine,    GameTime(3,  5), 4),
  new FingerprintCompleteBy(Terran.Marine,    GameTime(3, 20), 6),
  new FingerprintCompleteBy(Terran.Marine,    GameTime(3, 35), 8),
  new FingerprintCompleteBy(Terran.Marine,    GameTime(3, 50), 10),
  new FingerprintCompleteBy(Terran.Marine,    GameTime(4,  5), 12),
  new FingerprintArrivesBy(Terran.Marine,     GameTime(3, 10), 2),
  new FingerprintArrivesBy(Terran.Marine,     GameTime(3, 25), 4),
  new FingerprintArrivesBy(Terran.Marine,     GameTime(3, 40), 6),
  new FingerprintArrivesBy(Terran.Marine,     GameTime(3, 55), 8),
  new FingerprintArrivesBy(Terran.Marine,     GameTime(4, 10), 10),
  new FingerprintArrivesBy(Terran.Marine,     GameTime(4, 25), 12))