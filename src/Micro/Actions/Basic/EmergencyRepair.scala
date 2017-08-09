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
  
  def eligblePatients(unit: FriendlyUnitInfo): Iterable[UnitInfo] = {
    val dangerLifetime = 24 * 10
    unit.matchups.allies.filter(patient => patient.unitClass.isMechanical &&
      (unit.repairing && patient.hitPoints < patient.unitClass.maxHitPoints) ||
      patient.wounded                                                         ||
      patient.damageInLastSecond > patient.totalHealth / 10)
  }
}
