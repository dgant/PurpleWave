package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Fingerprint
import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchWorkers}
import ProxyBwapi.UnitInfo.UnitInfo

class FingerprintWorkerRush extends Fingerprint {
  
  override val sticky = true

  protected object UnitMatchAttackingWorker extends UnitMatchAnd(
      UnitMatchWorkers,
      (unit: UnitInfo) => {
        val distanceOurBase = unit.pixelDistanceTravelling(With.geography.ourMain.heart.pixelCenter)
        With.geography.startBases.forall(base =>
          !base.isOurMain
            | unit.pixelDistanceTravelling(base.heart.pixelCenter) > distanceOurBase
        )
      }
    )

  override protected def investigate: Boolean =
    (With.frame < GameTime(4, 0)() && With.units.countEnemy(UnitMatchAttackingWorker) > 2) ||
    (With.frame < GameTime(6, 0)() && With.units.countEnemy(UnitMatchAttackingWorker) > 4)
}
