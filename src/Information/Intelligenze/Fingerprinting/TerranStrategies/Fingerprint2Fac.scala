package Information.Intelligenze.Fingerprinting.TerranStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Terran

class Fingerprint2Fac extends FingerprintOr(
  With.fingerprints.twoFacVultures,
  new FingerprintCompleteBy(Terran.Factory, GameTime(4, 40), 2), // 4:10 normally
  new FingerprintAnd(
    new FingerprintNot(With.fingerprints.siegeExpand),
    new FingerprintNot(With.fingerprints.oneRaxFE),
    new FingerprintNot(With.fingerprints.fourteenCC),
    new FingerprintNFactories(2)))