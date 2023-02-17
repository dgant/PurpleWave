package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Terran
import Utilities.Time.GameTime

class FingerprintFD extends FingerprintAnd(
  With.fingerprints.oneFac,
  new FingerprintOr(
    new FingerprintArrivesBy(Terran.Marine, GameTime(5, 0), 5),
    new FingerprintCompleteBy(Terran.Marine, GameTime(4, 30), 5),
    new FingerprintCompleteBy(Terran.Marine, GameTime(6, 0), 6)))