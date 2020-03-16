package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Terran

class Fingerprint3Fac extends FingerprintOr(
  With.fingerprints.threeFacVultures,
  new FingerprintCompleteBy(Terran.Factory, GameTime(5, 0), 3), // 4:40 normally
  new FingerprintAnd(
    new FingerprintNot(With.fingerprints.siegeExpand),
    new FingerprintNot(With.fingerprints.oneRaxFE),
    new FingerprintNot(With.fingerprints.fourteenCC),
    new FingerprintNFactories(3)))