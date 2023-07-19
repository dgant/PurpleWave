package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintGasCompleteBy}
import Information.Fingerprinting.ZergStrategies.ZergTimings.TwelveHatch11Pool10Gas_GasCompleteBy
import Lifecycle.With
import Utilities.Time.Seconds

class Fingerprint12Hatch11Pool10Gas extends FingerprintAnd(
  With.fingerprints.twelveHatchPool,
  new FingerprintGasCompleteBy(TwelveHatch11Pool10Gas_GasCompleteBy + Seconds(15)))