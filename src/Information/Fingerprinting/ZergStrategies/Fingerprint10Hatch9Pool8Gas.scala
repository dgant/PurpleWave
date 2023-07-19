package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintGasCompleteBy}
import Information.Fingerprinting.ZergStrategies.ZergTimings.TenHatch9Pool8Gas_GasCompleteBy
import Lifecycle.With
import Utilities.Time.Seconds

class Fingerprint10Hatch9Pool8Gas extends FingerprintAnd(
  With.fingerprints.tenHatchPool,
  new FingerprintGasCompleteBy(TenHatch9Pool8Gas_GasCompleteBy + Seconds(15)))