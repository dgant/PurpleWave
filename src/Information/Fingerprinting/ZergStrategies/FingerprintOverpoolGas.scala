package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintGasCompleteBy}
import Information.Fingerprinting.ZergStrategies.ZergTimings.Overpool9Gas_GasCompleteBy
import Lifecycle.With
import Utilities.Time.Seconds

class FingerprintOverpoolGas extends FingerprintAnd(
  With.fingerprints.overpool,
  new FingerprintGasCompleteBy(Overpool9Gas_GasCompleteBy + Seconds(15)))
