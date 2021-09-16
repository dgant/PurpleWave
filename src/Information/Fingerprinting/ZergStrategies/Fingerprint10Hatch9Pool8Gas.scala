package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintGasCompleteBy}
import Information.Fingerprinting.Strategies.ZergTimings
import Lifecycle.With
import Utilities.Time.Seconds

class Fingerprint10Hatch9Pool8Gas extends FingerprintAnd(
  With.fingerprints.tenHatchPool,
  new FingerprintGasCompleteBy(ZergTimings.TenHatch9Pool8Gas_GasCompleteBy + Seconds(15)))