package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintGasCompleteBy}
import Information.Fingerprinting.Strategies.ZergTimings
import Lifecycle.With
import Utilities.Time.Seconds

class Fingerprint12Pool11Gas extends FingerprintAnd(
  With.fingerprints.twelvePool,
  new FingerprintGasCompleteBy(ZergTimings.TwelvePool11Gas_GasCompleteBy + Seconds(10)))