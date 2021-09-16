package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintCompleteBy, FingerprintNot}
import Information.Fingerprinting.Strategies.ZergTimings
import Lifecycle.With
import Planning.UnitMatchers.MatchHatchlike
import ProxyBwapi.Races.Zerg
import Utilities.Time.GameTime

class Fingerprint10Hatch9Pool extends FingerprintAnd(
  new FingerprintCompleteBy(MatchHatchlike, ZergTimings.TwelveHatch_HatchCompleteBy, 2),
  new FingerprintCompleteBy(Zerg.SpawningPool, GameTime(2, 40)),
  new FingerprintNot(
    With.fingerprints.twelvePool,
    With.fingerprints.overpool,
    With.fingerprints.ninePool),
)