package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Fingerprint
import Information.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Terran
import Utilities.Time.GameTime
import Utilities.UnitFilters.{IsAll, IsProxied}

class FingerprintProxyRax extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.fourteenCC),
  new FingerprintOr(
    new FingerprintCompleteBy(IsAll(Terran.Barracks, IsProxied), GameTime(5,  0)),
    new Fingerprint {
      override protected def investigate: Boolean = (
        With.frame > GameTime(1, 30)()
          && With.frame < GameTime(4, 0)()
          && With.units.countEnemy(Terran.Barracks, Terran.Refinery) == 0
          && With.scouting.enemyMainFullyScouted)
      override protected val reason: String = "Main empty"
  }))