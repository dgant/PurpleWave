package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic._
import Information.Fingerprinting.Strategies.ZergTimings
import Lifecycle.With
import Utilities.UnitFilters.IsHatchlike
import ProxyBwapi.Races.Zerg
import Utilities.Time.{Frames, Seconds}

class FingerprintOverpoolHatch extends FingerprintAnd(
  With.fingerprints.overpool,
  new FingerprintNot(With.fingerprints.overpoolGas),
  new FingerprintOr(
    new FingerprintCompleteBy(IsHatchlike, ZergTimings.Overpool11Hatch_HatchCompleteBy + Seconds(10)),
    new FingerprintGasEmptyUntil(ZergTimings.Overpool11Hatch_HatchCompleteBy - Frames(Zerg.Hatchery.buildFrames))))