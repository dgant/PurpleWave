package Micro.Heuristics.Targeting

import Micro.Intentions.Intention
import ProxyBwapi.UnitInfo.UnitInfo
import Startup.With

object Threats {
  def get(intent:Intention):Set[UnitInfo] = {
    With.units.inPixelRadius(
      intent.unit.pixelCenter,
      32 * 18)
      .filter(_.possiblyStillThere)
      .filter(_.canAttackRightNow(intent.unit))
      .filter(intent.unit.isEnemyOf)
      
  }
}
