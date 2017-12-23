package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object EmergencyRepair extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Terran.SCV)
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    val patients = eligiblePatients(unit)
    
    if (patients.isEmpty) return
    
    val patient = patients.minBy(_.totalHealth)
    
    With.commander.repair(unit, patient)
  }
  
  def eligiblePatients(repairer: FriendlyUnitInfo): Iterable[UnitInfo] = {
    repairer.teammates.filter(patient =>
      patient.unitClass.isMechanical    &&
      isCloseEnough(repairer, patient)  &&
      needsRepair(patient)              &&
      ! patient.moving                  &&
      ! patient.plagued
    )
  }
  
  def isCloseEnough(repairer: FriendlyUnitInfo, patient: UnitInfo): Boolean = {
    lazy val patientLifetime  = Math.max(patient.matchups.framesToLiveCurrently, patient.matchups.framesOfSafetyDiffused)
    lazy val canSaveBunker    = patient.is(Terran.Bunker) && repairer.framesToGetInRange(patient) < patientLifetime * 2
    lazy val canSavePlebian   = repairer.framesToGetInRange(patient) < patientLifetime
    lazy val repairerAdjacent = repairer.pixelsFromEdgeFast(patient) < 16.0
    lazy val patientImmobile  = ! patient.canMove
    lazy val repairerNearest  = patient.matchups.repairers.isEmpty  &&
      patient.framesToGetInRange(patient) < 24 * 4.0                &&
      ! patient.matchups.allies.exists(otherRepairer =>
        otherRepairer != repairer &&
        otherRepairer.pixelDistanceFast(patient) < repairer.pixelDistanceFast(patient))
    
    val output = (canSaveBunker || canSavePlebian) && (repairerAdjacent || patientImmobile || repairerNearest)
    output
  }
  
  def needsRepair(patient: UnitInfo): Boolean = {
    patient.canAttack && patient.totalHealth < patient.unitClass.maxTotalHealth ||
    patient.totalHealth < patient.unitClass.maxTotalHealth * 0.8 ||
    patient.matchups.dpfReceivingCurrently > 0.0
  }
}
