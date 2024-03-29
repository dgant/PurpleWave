package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintGasCompleteBy, FingerprintNot, FingerprintOr}
import Information.Fingerprinting.ZergStrategies.ZergTimings.ThreeHatchGasCompleteBy
import Lifecycle.With
import Utilities.Time.Seconds

class Fingerprint3HatchGas extends FingerprintAnd(
  new FingerprintNot(
    With.fingerprints.oneHatchGas,
    With.fingerprints.twoHatchGas),
  new FingerprintOr(
    With.fingerprints.twelveHatchHatch,
    With.fingerprints.twelveHatchPoolHatch,
    new FingerprintGasCompleteBy(ThreeHatchGasCompleteBy + Seconds(25))))
