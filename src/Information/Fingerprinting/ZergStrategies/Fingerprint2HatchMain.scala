package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.UnitMatchers.MatchHatchlike
import ProxyBwapi.UnitInfo.UnitInfo

class Fingerprint2HatchMain extends Fingerprint {
  override protected def investigate: Boolean = {
    hatcheries.forall(_.base.exists(With.scouting.enemyMain.contains)) && hatcheries.size > 1
  }

  override def reason: String = hatcheries.mkString(", ")

  override val sticky = true

  protected def hatcheries: Iterable[UnitInfo] = With.units.enemy.filter(MatchHatchlike)
}
