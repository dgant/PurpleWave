package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintGasCompleteBy, FingerprintNot, FingerprintOr}
import Information.Fingerprinting.Strategies.ZergTimings
import Lifecycle.With
import Utilities.Time.Seconds

class Fingerprint2HatchGas extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.oneHatchGas),
  new FingerprintOr(
    new FingerprintGasCompleteBy(ZergTimings.ThreeHatchGasCompleteBy - Seconds(5)),
    With.fingerprints.tenHatchPoolGas,
    With.fingerprints.twelveHatchPoolGas))