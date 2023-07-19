package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintCompleteBy, FingerprintOr}
import Lifecycle.With
import Utilities.UnitFilters.IsHatchlike
import Utilities.Time.Seconds

class Fingerprint10Hatch extends FingerprintOr(
  new FingerprintCompleteBy(IsHatchlike, ZergTimings.TwelveHatch_HatchCompleteBy - Seconds(10), 2),
  With.fingerprints.tenHatchPool)