package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Terran
import Utilities.Time.GameTime

class Fingerprint3Fac extends FingerprintOr(
  With.fingerprints.threeFacVultures,
  new FingerprintCompleteBy(Terran.Factory, GameTime(5, 0), 3), // 4:40 normally
  new FingerprintNFactories(3))