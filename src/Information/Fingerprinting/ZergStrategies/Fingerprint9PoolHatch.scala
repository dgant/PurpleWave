package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic._
import Information.Fingerprinting.Strategies.ZergTimings
import Lifecycle.With
import Utilities.UnitFilters.IsHatchlike
import Utilities.Time.{GameTime, Seconds}

class Fingerprint9PoolHatch extends FingerprintAnd(
  With.fingerprints.ninePool,
  new FingerprintNot(With.fingerprints.ninePoolGas),
  new FingerprintOr(
    new FingerprintCompleteBy(IsHatchlike, ZergTimings.NinePool13Hatch_HatchCompleteBy + Seconds(5)),
    new FingerprintGasEmptyUntil(GameTime(1, 56))))