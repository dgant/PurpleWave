package Micro.Actions.Combat.Maneuvering

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.Potshot
import Micro.Actions.Combat.Decisionmaking.Engage
import Micro.Actions.Combat.Specialized.CarrierRetreat
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

    CarrierRetreat.delegate(unit)
  
    lazy val threatsAllMelee    = unit.matchups.threats.forall(_.melee)
    lazy val alreadyHome        = unit.pixelCenter.zone == unit.action.origin.zone
    lazy val holdingFormation   = unit.action.toForm.exists(unit.pixelDistanceFast(_) < 4.0)
    lazy val canTakeFreeShots   = unit.matchups.threatsInRange.isEmpty
    lazy val slowerThanThreats  = unit.matchups.threats.forall(_.topSpeedChasing > unit.topSpeed)
    lazy val trapped            = unit.damageInLastSecond > 0 &&
      unit.matchups.threatsViolent.count(threat =>
        threat.melee
          && threat.topSpeedChasing > unit.topSpeed
          && threat.pixelDistanceFast(unit) < 48.0) > 2
    
    if (canTakeFreeShots || slowerThanThreats || trapped) {
      Potshot.delegate(unit)
    }
    
    if (unit.melee && threatsAllMelee && holdingFormation) {
      With.commander.hold(unit)
    }
    
    if (alreadyHome) {
      Engage.consider(unit)
    }
    else {
      // TODO: Dodge! Kite!
      Travel.delegate(unit)
    }
  }
}
