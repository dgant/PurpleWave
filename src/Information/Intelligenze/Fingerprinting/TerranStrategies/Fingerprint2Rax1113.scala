package Information.Intelligenze.Fingerprinting.TerranStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Terran

class Fingerprint2Rax1113 extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.bbs),
  new FingerprintOr(
    new FingerprintCompleteBy(Terran.Barracks,  GameTime(3,  0), 2), // Normally about 2:45
    new FingerprintCompleteBy(Terran.Marine,    GameTime(3,  5), 3),
    new FingerprintCompleteBy(Terran.Marine,    GameTime(3, 20), 5),
    new FingerprintCompleteBy(Terran.Marine,    GameTime(3, 40), 7),
    new FingerprintArrivesBy(Terran.Marine,     GameTime(3, 40), 3),
    new FingerprintArrivesBy(Terran.Marine,     GameTime(3, 55), 5),
    new FingerprintArrivesBy(Terran.Marine,     GameTime(4, 10), 7)))
