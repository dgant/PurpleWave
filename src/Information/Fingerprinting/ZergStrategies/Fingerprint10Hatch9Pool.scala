package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintCompleteBy, FingerprintNot}
import Information.Fingerprinting.Strategies.ZergTimings
import Lifecycle.With
import Utilities.UnitFilters.IsHatchlike
import ProxyBwapi.Races.Zerg
import Utilities.Time.GameTime

class Fingerprint10Hatch9Pool extends FingerprintAnd(
  new FingerprintCompleteBy(IsHatchlike, ZergTimings.TwelveHatch_HatchCompleteBy, 2),
  new FingerprintCompleteBy(Zerg.SpawningPool, GameTime(2, 40)),
  new FingerprintNot(
    With.fingerprints.twelvePool,
    With.fingerprints.overpool,
    With.fingerprints.ninePool),
)