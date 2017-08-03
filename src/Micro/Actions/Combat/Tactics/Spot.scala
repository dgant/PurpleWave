package Micro.Actions.Combat.Tactics

import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Spot extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove && unit.flying
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val blindTanks = unit.matchups.allies.filter(tank =>
      tank.is(Terran.SiegeTankSieged)                   &&
      ! tank.matchups.targetsInRange.exists(_.visible)  &&
      tank.matchups.targetsInRange.exists( ! _.visible))
    
    if (blindTanks.nonEmpty) {
      val tank = blindTanks.maxBy(_.matchups.vpfDealingDiffused)
      val target = tank.pixelCenter.project(tank.matchups.targetsInRange.minBy(_.pixelDistanceFast(tank)).pixelCenter, 32.0 * 4.0)
      unit.agent.toTravel = Some(target)
    }
  }
}
