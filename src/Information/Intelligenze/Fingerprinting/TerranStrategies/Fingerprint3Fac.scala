package Information.Intelligenze.Fingerprinting.TerranStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Terran

class Fingerprint3Fac extends FingerprintOr(
  With.fingerprints.threeFacVultures,
  new FingerprintCompleteBy(Terran.Factory, GameTime(5, 0), 3), // 4:40 normally
  new FingerprintAnd(
    new FingerprintNot(With.fingerprints.siegeExpand),
    new FingerprintNFactories(3)))