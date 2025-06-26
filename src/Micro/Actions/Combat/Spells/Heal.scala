package Micro.Actions.Combat.Spells

import Mathematics.Maff
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Heal extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = Terran.Medic(unit) && unit.battle.isDefined
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val targets = validTargets(unit)
    val target  = Maff.minBy(targets)(patient =>
      if (unit.battle.isDefined && unit.matchups.threats.nonEmpty)
        patient.pixelDistanceEdge(unit)
      else
        patient.pixelDistanceCenter(unit.agent.destinationNext()))
    
    target.foreach(someTarget => {
      unit.agent.perch.set(unit.pixel.project(someTarget.pixel, unit.pixelDistanceEdge(someTarget) - 16.0))
      if (someTarget.pixelDistanceCenter(unit) < 96.0) {
        Commander.attackMove(unit)
      }
    })
  }
  
  private def validTargets(medic: FriendlyUnitInfo): Seq[UnitInfo] = {
    medic
      .tileArea.expand(2)
      .tiles
      .flatMap(_.units)
      .filter(patient =>
          patient.unitClass.isOrganic
        && patient.healers.forall(medic==))
  }
  
  private def targetValue(medic: FriendlyUnitInfo, patient: UnitInfo): Double = {
    val damageScore     = (1.1 - patient.hitPoints.toFloat / patient.unitClass.maxHitPoints)
    val proximityScore  = 1.0 / (1 + medic.pixelDistanceCenter(patient))
    val threatScore     = 1.0 / Math.max(1.0, patient.matchups.pixelsToThreatRange.getOrElse(0.0))
    damageScore * proximityScore * threatScore
  }
}
