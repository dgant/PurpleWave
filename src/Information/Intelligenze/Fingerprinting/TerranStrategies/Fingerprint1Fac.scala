package Information.Intelligenze.Fingerprinting.TerranStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Terran

class Fingerprint1Fac extends FingerprintAnd(
  new FingerprintOr(
    new FingerprintArrivesBy(Terran.Vulture, GameTime(5, 0), 1),
    new FingerprintCompleteBy(Terran.Factory, GameTime(4, 30), 1)), // 4:10 normally
  new FingerprintNot(With.fingerprints.siegeExpand),
  new FingerprintNot(With.fingerprints.oneRaxFE),
  new FingerprintNot(With.fingerprints.fourteenCC),
  new FingerprintNot(With.fingerprints.twoFac))