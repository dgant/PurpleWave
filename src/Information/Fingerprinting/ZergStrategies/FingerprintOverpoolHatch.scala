package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic._
import Information.Fingerprinting.Strategies.ZergTimings
import Lifecycle.With
import Planning.UnitMatchers.MatchHatchlike
import ProxyBwapi.Races.Zerg
import Utilities.Time.{Frames, Seconds}

class FingerprintOverpoolHatch extends FingerprintAnd(
  With.fingerprints.ninePool,
  new FingerprintNot(With.fingerprints.overpoolGas),
  new FingerprintOr(
    new FingerprintCompleteBy(MatchHatchlike, ZergTimings.Overpool11Hatch_HatchCompleteBy + Seconds(10)),
    new FingerprintGasEmptyUntil(ZergTimings.Overpool11Hatch_HatchCompleteBy - Frames(Zerg.Hatchery.buildFrames))))