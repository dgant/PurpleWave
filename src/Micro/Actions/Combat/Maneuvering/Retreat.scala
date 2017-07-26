package Micro.Actions.Combat.Maneuvering

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.Potshot
import Micro.Actions.Commands.Travel
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Retreat extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMoveThisFrame &&
    unit.pixelCenter.zone != unit.action.origin.zone &&
    unit.matchups.threats.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
  
    unit.action.toTravel = Some(unit.action.origin)
  
    lazy val zone               = unit.pixelCenter.zone
    lazy val originZone         = unit.action.origin.zone
    lazy val exitToOrigin       = zone.pathTo(originZone).flatMap(_.steps.headOption.map(_.edge.centerPixel))
    lazy val threatsAllMelee    = unit.matchups.threats.forall(_.melee)
    lazy val alreadyHome        = zone == originZone
    lazy val holdingFormation   = unit.action.toForm.exists(unit.pixelDistanceFast(_) < 4.0)
    lazy val canTakeFreeShots   = unit.matchups.threatsInRange.isEmpty
    lazy val slowerThanThreats  = unit.matchups.threats.forall(_.topSpeedChasing > unit.topSpeed)
    lazy val trapped            = unit.damageInLastSecond > 0 && (exitToOrigin.isEmpty || unit.matchups.framesToLiveCurrently < unit.framesToTravelTo(exitToOrigin.get))
    
    if (canTakeFreeShots || slowerThanThreats || alreadyHome || trapped) {
      Potshot.delegate(unit)
    }
    
    if (unit.melee && threatsAllMelee && holdingFormation) {
      With.commander.hold(unit)
    }
    else if (alreadyHome || unit.flying || trapped) {
      Avoid.delegate(unit)
    }
    else {
      Travel.delegate(unit)
    }
  }
}
