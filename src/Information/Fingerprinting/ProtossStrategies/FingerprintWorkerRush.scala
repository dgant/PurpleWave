package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchWarriors, UnitMatchWorkers}
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.GameTime

class FingerprintWorkerRush extends Fingerprint {
  
  override val sticky = true

  protected object UnitMatchAttackingWorker extends UnitMatchAnd(
      UnitMatchWorkers,
      (unit: UnitInfo) => {
        val distanceOurBase = unit.pixelDistanceTravelling(With.geography.ourMain.heart.pixelCenter)
        With.geography.startBases.forall(base =>
          base.isOurMain || unit.pixelDistanceTravelling(base.heart.pixelCenter) > distanceOurBase
        )
      }
    )

  override protected def investigate: Boolean = (
     (
      (With.frame < GameTime(4, 0)() && With.units.countEnemy(UnitMatchAttackingWorker) > 2) ||
      (With.frame < GameTime(6, 0)() && With.units.countEnemy(UnitMatchAttackingWorker) > 4)
     ) && ! With.units.existsEnemy(UnitMatchWarriors)
  )
}
