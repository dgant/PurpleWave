package Micro.Actions.Basic

import Information.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object EmergencyRepair extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.is(Terran.SCV)
    && With.self.minerals + With.economy.ourIncomePerFrameMinerals * GameTime(0, 10)() > 25
  )
  
  override def perform(unit: FriendlyUnitInfo) {
    val patients = eligiblePatients(unit)
    
    if (patients.isEmpty) return
    
    val patient = patients.minBy(_.pixelDistanceEdge(unit))
    
    unit.agent.toRepair = Some(patient)
    With.commander.repair(unit, patient)
  }
  
  def eligiblePatients(repairer: FriendlyUnitInfo): Seq[UnitInfo] =
    repairer.teammates.view.filter(patient =>
      patient != repairer
      && patient.complete
      && patient.unitClass.isMechanical
      && isCloseEnough(repairer, patient)
      && needsRepair(repairer, patient)
      && ! patient.is(Terran.SCV)
      && ! patient.moving
      && ! patient.plagued
    ).toSeq
  
  def isCloseEnough(repairer: FriendlyUnitInfo, patient: UnitInfo): Boolean = {
    if (repairer.pixelDistanceEdge(patient) > 32.0 * 30.0) return false
    
    lazy val patientLifetime  = Math.max(patient.matchups.framesToLive, patient.matchups.framesOfSafety)
    lazy val canSaveBunker    = patient.is(Terran.Bunker) && repairer.framesToGetInRange(patient) < patientLifetime * 2
    lazy val canSavePlebian   = repairer.framesToGetInRange(patient) < patientLifetime
    lazy val repairerAdjacent = repairer.pixelDistanceEdge(patient) < 16.0
    lazy val patientImmobile  = ! patient.canMove
    lazy val repairerNearest  = (patient.matchups.repairers.isEmpty
      && patient.framesToGetInRange(patient) < 24 * 4.0
      && ! patient.matchups.allies.exists(otherRepairer =>
        otherRepairer != repairer &&
        otherRepairer.pixelDistanceEdge(patient) < repairer.pixelDistanceEdge(patient)))
    
    val output = (canSaveBunker || canSavePlebian) && (repairerAdjacent || patientImmobile || repairerNearest)
    output
  }
  
  def needsRepair(repairer: FriendlyUnitInfo, patient: UnitInfo): Boolean = {
    lazy val docsRepairingNow = patient.matchups.repairers.size
    lazy val repairPerFrame   = 0.9 * patient.unitClass.maxTotalHealth / patient.unitClass.buildFrames
    lazy val dpfRepairingNow  = repairPerFrame * docsRepairingNow
    
    lazy val isAlreadyPatient = patient.matchups.repairers.contains(repairer)
    lazy val isDefense        = patient.canAttack
    lazy val isDamaged        = patient.totalHealth < patient.unitClass.maxTotalHealth
    lazy val isDamagedBadly   = patient.totalHealth < patient.unitClass.maxTotalHealth * 0.5
    lazy val isDamagedDefense = isDamaged && isDefense
    lazy val needsMoreRepair  = docsRepairingNow == 0 || patient.matchups.dpfReceiving >= repairPerFrame * 1.8
    lazy val needsRepairSoon  = isDefense && patient.matchups.threats.nonEmpty && docsRepairingNow < 2
    
    val output = (isAlreadyPatient || isDamagedBadly || isDamagedDefense) && (needsMoreRepair || needsRepairSoon)
    
    output
  }
}
