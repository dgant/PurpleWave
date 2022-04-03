package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic._
import Information.Fingerprinting.Strategies.ZergTimings
import Lifecycle.With
import Utilities.UnitMatchers.MatchHatchlike
import ProxyBwapi.Races.Zerg
import Utilities.Time.{Frames, Seconds}

class Fingerprint12Hatch11Pool13Hatch extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.twelveHatchHatch),
  new FingerprintNot(With.fingerprints.twelveHatchPoolGas),
  new FingerprintOr(
    // Match if it's any 3-hatch slower than 12h13h
    new FingerprintCompleteBy(MatchHatchlike, ZergTimings.TwelveHatch11Pool13Hatch_HatchCompleteBy + Seconds(10), 3),
    // Match 12h11p followed by no gas (maybe there's a 3rd hatch we haven't seen, especially if it's at an expansion)
    new FingerprintAnd(
      With.fingerprints.twelveHatchPool,
      new FingerprintGasEmptyUntil(ZergTimings.TwelveHatch12Pool_GasCompleteBy - Frames(Zerg.Extractor.buildFrames) + Seconds(15)))))