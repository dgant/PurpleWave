package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic._
import Information.Fingerprinting.Strategies.ZergTimings
import Lifecycle.With
import Utilities.Time.Seconds

class Fingerprint1HatchGas extends FingerprintOr(
  new FingerprintGasCompleteBy(ZergTimings.TwoHatchGasCompleteBy - Seconds(5)),
  With.fingerprints.ninePoolGas,
  With.fingerprints.overpoolGas,
  With.fingerprints.twelvePoolGas)