package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintGasCompleteBy}
import Information.Fingerprinting.ZergStrategies.ZergTimings.TwelvePool11Gas_GasCompleteBy
import Lifecycle.With
import Utilities.Time.Seconds

class Fingerprint12Pool11Gas extends FingerprintAnd(
  With.fingerprints.twelvePool,
  new FingerprintGasCompleteBy(TwelvePool11Gas_GasCompleteBy + Seconds(10)))