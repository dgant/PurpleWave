package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic._
import Information.Fingerprinting.ZergStrategies.ZergTimings.TwoHatchGasCompleteBy
import Lifecycle.With
import Utilities.Time.Seconds
import bwapi.Race

class Fingerprint1HatchGas extends FingerprintAnd(
  new FingerprintRace(Race.Zerg),
  new FingerprintOr(
    new FingerprintGasCompleteBy(TwoHatchGasCompleteBy - Seconds(5)),
    With.fingerprints.ninePoolGas,
    With.fingerprints.overpoolGas,
    With.fingerprints.twelvePoolGas))