package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintGasCompleteBy, FingerprintNot, FingerprintOr}
import Information.Fingerprinting.Strategies.ZergTimings
import Lifecycle.With
import Utilities.Time.Seconds

class Fingerprint3HatchGas extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.oneHatchGas),
  new FingerprintNot(With.fingerprints.twoHatchGas),
  new FingerprintOr(
    With.fingerprints.twelveHatchHatch,
    With.fingerprints.twelveHatchPoolHatch,
    new FingerprintGasCompleteBy(ZergTimings.ThreeHatchGasCompleteBy + Seconds(25))))
