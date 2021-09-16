package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Terran
import Utilities.Time.GameTime

class Fingerprint2Rax1113 extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.bbs),
  new FingerprintNot(With.fingerprints.oneRaxFE),
  new FingerprintNot(With.fingerprints.fourteenCC),
  new FingerprintOr(
    new FingerprintCompleteBy(Terran.Barracks,  GameTime(2, 55), 2), // Normally about 2:45
    new FingerprintCompleteBy(Terran.Marine,    GameTime(2, 55), 3),
    new FingerprintCompleteBy(Terran.Marine,    GameTime(3, 15), 5),
    new FingerprintCompleteBy(Terran.Marine,    GameTime(3, 35), 7),
    new FingerprintCompleteBy(Terran.Marine,    GameTime(3, 55), 9),
    new FingerprintCompleteBy(Terran.Marine,    GameTime(4, 10), 11),
    new FingerprintCompleteBy(Terran.Marine,    GameTime(4, 25), 13),
    new FingerprintArrivesBy(Terran.Marine,     GameTime(3, 40), 3),
    new FingerprintArrivesBy(Terran.Marine,     GameTime(4,  0), 5),
    new FingerprintArrivesBy(Terran.Marine,     GameTime(4, 20), 7),
    new FingerprintArrivesBy(Terran.Marine,     GameTime(4, 40), 9),
    new FingerprintArrivesBy(Terran.Marine,     GameTime(5,  0), 11)))
