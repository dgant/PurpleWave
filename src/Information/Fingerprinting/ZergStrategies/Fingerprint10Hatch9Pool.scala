package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintCompleteBy, FingerprintNot}
import Information.Fingerprinting.ZergStrategies.ZergTimings.{TwelveHatch11Pool_PoolCompleteBy, TwelveHatch_HatchCompleteBy}
import Lifecycle.With
import ProxyBwapi.Races.Zerg
import Utilities.UnitFilters.IsHatchlike

class Fingerprint10Hatch9Pool extends FingerprintAnd(
  new FingerprintCompleteBy(IsHatchlike, TwelveHatch_HatchCompleteBy, 2),
  new FingerprintCompleteBy(Zerg.SpawningPool, TwelveHatch11Pool_PoolCompleteBy),
  new FingerprintNot(
    With.fingerprints.twelvePool,
    With.fingerprints.overpool,
    With.fingerprints.ninePool),
)