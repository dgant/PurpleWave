package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Fingerprint
import Information.Fingerprinting.Generic.{FingerprintAnd, FingerprintCompleteBy, FingerprintNot, FingerprintOr}
import Information.Fingerprinting.ZergStrategies.ZergTimings.{TenHatch9Pool_PoolCompleteBy, TwelveHatch_HatchCompleteBy}
import Lifecycle.With
import ProxyBwapi.Races.Zerg
import Utilities.Time.Seconds

class FingerprintHatchFirst extends FingerprintAnd(

  new FingerprintNot(
    With.fingerprints.fourPool,
    With.fingerprints.ninePool,
    With.fingerprints.overpool,
    With.fingerprints.twelvePool),

  new FingerprintOr(
    With.fingerprints.tenHatch,
    new FingerprintCompleteBy(IsNonStartingHatch, TwelveHatch_HatchCompleteBy + Seconds(10)),
    new Fingerprint {
      override protected def investigate: Boolean = {
        With.units.enemy.exists(u => Zerg.SpawningPool(u) && ! u.complete && u.completionFrame > TenHatch9Pool_PoolCompleteBy() - Seconds(3)())
      }
      override val sticky = true
      override val toString: String = "LatePool"
    }))
