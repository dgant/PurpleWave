package Micro.Actions.Combat.Tactics

import Micro.Actions.Action
import Micro.Actions.Commands.Move
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object Spot extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.flying
    && unit.matchups.targets.isEmpty
    && (unit.matchups.framesOfSafetyDiffused > 0 || unit.totalHealth > 500)
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val shooters          = unit.squadmates.filter(_.canAttack)
    val maxRange          = ByOption.max(shooters.map(_.pixelRangeMax)).getOrElse(0.0)
    val snipers           = shooters.filter(_.pixelRangeMax >= maxRange)
    val hiders            = snipers.flatMap(sniper => sniper.matchups.targets).toSet.filter(enemy => ! enemy.visible)
    val marginPixels      = unit.sightRangePixels / 2
    
    if (snipers.nonEmpty && hiders.nonEmpty) {
      val nearestHider  = hiders.minBy(_.pixelDistanceFast(unit))
      val nearestSniper = snipers.minBy(_.pixelDistanceFast(nearestHider))
      unit.agent.toTravel = Some(nearestHider.pixelCenter.project(nearestSniper.pixelCenter, unit.sightRangePixels - 32))
      Move.delegate(unit)
    }
  }
}
