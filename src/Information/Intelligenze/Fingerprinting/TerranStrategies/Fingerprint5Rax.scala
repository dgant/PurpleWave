package Information.Intelligenze.Fingerprinting.TerranStrategies

import Information.Intelligenze.Fingerprinting.Generic.{FingerprintArrivesBy, FingerprintCompleteBy, FingerprintOr, GameTime}
import ProxyBwapi.Races.Terran

class Fingerprint5Rax extends FingerprintOr(
  new FingerprintCompleteBy(Terran.Barracks,  GameTime(1, 45),  1), // Normally 1:35
  new FingerprintCompleteBy(Terran.Marine,    GameTime(2, 0),   1),
  new FingerprintArrivesBy(Terran.Marine,     GameTime(2, 30),  1),
  new FingerprintArrivesBy(Terran.Marine,     GameTime(2, 45),  2),
  new FingerprintArrivesBy(Terran.Marine,     GameTime(3,  0),  3),
  new FingerprintArrivesBy(Terran.Marine,     GameTime(3, 10),  4) // Cut a little earlier to avoid confusion with BBS
)