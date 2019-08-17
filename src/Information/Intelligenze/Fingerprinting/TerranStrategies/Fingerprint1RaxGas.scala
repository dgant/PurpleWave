package Information.Intelligenze.Fingerprinting.TerranStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Terran

class Fingerprint1RaxGas extends FingerprintOr(
  With.fingerprints.oneFac,
  new FingerprintCompleteBy(Terran.Refinery, GameTime(2, 45), 1))