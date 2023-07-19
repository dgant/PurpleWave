package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic._
import Information.Fingerprinting.ZergStrategies.ZergTimings.Overpool8Gas_GasCompleteBy
import Utilities.Time.Seconds

class Fingerprint9PoolGas extends FingerprintGasCompleteBy(Overpool8Gas_GasCompleteBy - Seconds(5))
