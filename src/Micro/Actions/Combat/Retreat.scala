package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Travel
import Micro.Behaviors.MovementProfiles
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Retreat extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMoveThisFrame &&
    unit.pixelCenter.zone != unit.action.origin.zone &&
    unit.matchups.threats.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
  
    unit.action.toTravel = Some(unit.action.origin)
    
    // Carriers have their own wonky retreat logic
    //
    CarrierRetreat.delegate(unit)
    
    val slowerThanThreats = unit.matchups.threats.forall(_.topSpeed > unit.topSpeed)
    lazy val trapped = unit.damageInLastSecond > 0 && unit.matchups.threatsViolent.exists(threat =>
      threat.topSpeed * 0.8 > unit.topSpeed
      && threat.inRangeToAttackFast(unit))

    if (slowerThanThreats || trapped) {
      Potshot.delegate(unit)
    }
    
    // If we're a melee unit trying to defend a choke against other melee units, hold the line!
    //
    lazy val threatsAllMelee = unit.matchups.threats.forall(_.melee)
    
    if (unit.melee && threatsAllMelee && unit.pixelDistanceFast(unit.action.origin) < 16.0) {
      Potshot.delegate(unit)
      Travel.delegate(unit)
    }
    
    if (unit.pixelDistanceFast(unit.action.origin) < 16.0) {
      unit.action.movementProfile = MovementProfiles.avoid
      Potshot.delegate(unit)
      Engage.consider(unit)
    }
    
    Travel.delegate(unit)
  }
}
