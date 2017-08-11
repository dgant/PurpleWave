package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object EmergencyRepair extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Terran.SCV) && eligblePatients(unit).nonEmpty
    
  }
  
  override def perform(unit: FriendlyUnitInfo) {
  
    val patients = eligblePatients(unit)
    
    if (patients.isEmpty) return
    
    val patient = patients.minBy(_.totalHealth)
    
    With.commander.repair(unit, patient)
  }
  
  def eligblePatients(repairer: FriendlyUnitInfo): Iterable[UnitInfo] = {
    repairer.matchups.allies.filter(patient =>
      patient.unitClass.isMechanical &&
      (patient.pixelDistanceFast(repairer) < 32 || ! patient.canMove) &&
      (
        (repairer.repairing && patient.hitPoints < patient.unitClass.maxHitPoints)  ||
        patient.wounded                                                             ||
        patient.damageInLastSecond > patient.totalHealth / 10
      ) &&
      ! patient.moving &&
      ! patient.plagued
    )
  }
}
