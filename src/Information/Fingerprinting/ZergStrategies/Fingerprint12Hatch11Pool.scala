package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintCompleteBy, FingerprintNot, FingerprintOr}
import Information.Fingerprinting.Strategies.ZergTimings
import Lifecycle.With
import ProxyBwapi.Races.Zerg
import Utilities.Time.Seconds

class Fingerprint12Hatch11Pool extends FingerprintOr(
  With.fingerprints.twelveHatchPoolHatch,
  new FingerprintAnd(
    With.fingerprints.twelveHatch,
    new FingerprintNot(With.fingerprints.twelveHatchHatch),
    new FingerprintCompleteBy(Zerg.SpawningPool, ZergTimings.TwelveHatch13Hatch12Pool_PoolCompleteBy - Seconds(3))))