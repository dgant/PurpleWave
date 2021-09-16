package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.UnitMatchers.{MatchAnd, MatchWarriors, MatchWorker}
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, UnitInfo}
import Utilities.Time.GameTime

class FingerprintWorkerRush extends Fingerprint {

  protected object MatchAttackingWorker extends MatchAnd(
      MatchWorker,
      (unit: UnitInfo) => {
        val distanceOurBase = unit.pixelDistanceTravelling(With.geography.ourMain.heart.center)
        With.geography.startBases.forall(base =>
          base.isOurMain || unit.pixelDistanceTravelling(base.heart.center) > distanceOurBase
        )
      }
    )

  override protected def investigate: Boolean = {
    val attackingWorkerCount = attackingWorkers.size
    if (With.units.existsEnemy(MatchWarriors)) return false
    if (With.frame < GameTime(4, 0)() && attackingWorkerCount > 2) return true
    if (With.frame < GameTime(6, 0)() && attackingWorkerCount > 4) return true
    false
  }

  override val sticky = true

  override def reason: String = f"[${attackingWorkers.mkString(", ")}]"

  protected def attackingWorkers: Iterable[ForeignUnitInfo] = With.units.enemy.filter(MatchAttackingWorker)
}
