package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.UnitMatchers.{MatchAnd, MatchWarriors, MatchWorkers}
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.GameTime

class FingerprintWorkerRush extends Fingerprint {
  
  override val sticky = true

  protected object MatchAttackingWorker$ extends MatchAnd(
      MatchWorkers,
      (unit: UnitInfo) => {
        val distanceOurBase = unit.pixelDistanceTravelling(With.geography.ourMain.heart.pixelCenter)
        With.geography.startBases.forall(base =>
          base.isOurMain || unit.pixelDistanceTravelling(base.heart.pixelCenter) > distanceOurBase
        )
      }
    )

  override protected def investigate: Boolean = (
     (
      (With.frame < GameTime(4, 0)() && With.units.countEnemy(MatchAttackingWorker$) > 2) ||
      (With.frame < GameTime(6, 0)() && With.units.countEnemy(MatchAttackingWorker$) > 4)
     ) && ! With.units.existsEnemy(MatchWarriors)
  )
}
