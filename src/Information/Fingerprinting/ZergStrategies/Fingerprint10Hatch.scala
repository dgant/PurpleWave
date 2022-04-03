package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintCompleteBy, FingerprintOr}
import Information.Fingerprinting.Strategies.ZergTimings
import Lifecycle.With
import Utilities.UnitMatchers.MatchHatchlike
import Utilities.Time.Seconds

class Fingerprint10Hatch extends FingerprintOr(
  new FingerprintCompleteBy(MatchHatchlike, ZergTimings.TwelveHatch_HatchCompleteBy - Seconds(10), 2),
  With.fingerprints.tenHatchPool)