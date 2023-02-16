package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Terran
import Utilities.Time.GameTime

class Fingerprint1Fac extends FingerprintAnd(
  new FingerprintOr(
    // Factory timings:
    // 3:10 off 10 gas
    // 3:30 standard
    // SAIDA's siege expand starts CC @ 3:52, so we cut off the latest Factory (50s) timing before then
    new FingerprintCompleteBy(Terran.Factory,           GameTime(4, 30), 1),
    new FingerprintCompleteBy(Terran.MachineShop,       GameTime(4, 55), 1),
    new FingerprintCompleteBy(Terran.Vulture,           GameTime(4, 49), 1),
    new FingerprintCompleteBy(Terran.SiegeTankUnsieged, GameTime(5,  2), 1),
    new FingerprintCompleteBy(Terran.SiegeTankSieged,   GameTime(5, 45), 1),
    new FingerprintArrivesBy(Terran.Vulture,            GameTime(5,  5), 1)),
  new FingerprintNot(With.fingerprints.bbs),
  new FingerprintNot(With.fingerprints.twoRax1113),
  new FingerprintNot(With.fingerprints.oneRaxFE),
  new FingerprintNot(With.fingerprints.fourteenCC),
  new FingerprintNot(With.fingerprints.twoFac),
  new FingerprintNot(With.fingerprints.threeFac))