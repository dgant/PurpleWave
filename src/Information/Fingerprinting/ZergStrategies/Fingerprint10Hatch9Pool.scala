package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintCompleteBy, FingerprintNot}
import Information.Fingerprinting.ZergStrategies.ZergTimings.{Latest_TenHatch_PoolCompleteBy, TwelveHatch_HatchCompleteBy}
import Lifecycle.With
import ProxyBwapi.Races.Zerg
import Utilities.Time.Seconds
import Utilities.UnitFilters.IsHatchlike

class Fingerprint10Hatch9Pool extends FingerprintAnd(
  new FingerprintCompleteBy(IsHatchlike, TwelveHatch_HatchCompleteBy - Seconds(10)(), 2),
  new FingerprintCompleteBy(Zerg.SpawningPool, Latest_TenHatch_PoolCompleteBy),
  new FingerprintNot(
    With.fingerprints.twelvePool,
    With.fingerprints.overpool,
    With.fingerprints.ninePool),
)