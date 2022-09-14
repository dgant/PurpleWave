package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintArrivesBy, FingerprintCompleteBy, FingerprintNot, FingerprintOr}
import Lifecycle.With
import ProxyBwapi.Races.Terran
import Utilities.Time.GameTime

class Fingerprint8Rax extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.fiveRax),
  new FingerprintOr(
    With.fingerprints.bbs,
    new FingerprintCompleteBy(Terran.Barracks,  GameTime(2, 35),  1),
    new FingerprintCompleteBy(Terran.Marine,    GameTime(2, 50),  1),
    new FingerprintCompleteBy(Terran.Marine,    GameTime(3,  5),  2),
    new FingerprintCompleteBy(Terran.Marine,    GameTime(3, 20),  3),
    new FingerprintCompleteBy(Terran.Marine,    GameTime(3, 35),  4),
    new FingerprintArrivesBy(Terran.Marine,     GameTime(3, 20),  1),
    new FingerprintArrivesBy(Terran.Marine,     GameTime(3, 35),  2),
    new FingerprintArrivesBy(Terran.Marine,     GameTime(3, 50),  3),
    new FingerprintArrivesBy(Terran.Marine,     GameTime(4,  5),  4)))