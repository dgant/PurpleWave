package Micro.Actions.Combat.Spells

import Mathematics.Maff
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Heal extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.is(Terran.Medic)
    && (unit.battle.isDefined || unit.agent.toReturn.isEmpty)
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val targets = validTargets(unit)
    val target  = Maff.minBy(targets)(patient =>
      if (unit.battle.isDefined && unit.matchups.threats.nonEmpty)
        patient.pixelDistanceEdge(unit)
      else
        patient.pixelDistanceCenter(unit.agent.destination))
    
    target.foreach(someTarget => {
      unit.agent.toTravel = Some(unit.pixel.project(someTarget.pixel, unit.pixelDistanceEdge(someTarget) - 16.0))
      if (someTarget.pixelDistanceCenter(unit) < 96.0) {
        Commander.attackMove(unit)
      }
    })
  }
  
  private def validTargets(unit: FriendlyUnitInfo): Vector[UnitInfo] = {
    unit
      .alliesSquad
      .filter(u =>
        u.unitClass.isOrganic
        && ! u.beingHealed
        && ! u.is(Terran.Medic)
        && (u.hitPoints < u.unitClass.maxHitPoints|| u.matchups.threats.nonEmpty))

      .toVector
  }
  
  private def targetValue(medic: FriendlyUnitInfo, patient: UnitInfo): Double = {
    val distancePixels  = medic.pixelDistanceEdge(patient)
    val distanceFrames  = Maff.nanToInfinity(distancePixels / medic.topSpeed)
    val lifetimeFrames  = patient.matchups.framesToLive
    val damage          = patient.unitClass.maxHitPoints - patient.hitPoints
    val safety          = Maff.signum(Math.max(0.0, patient.matchups.framesOfSafety))
    val valueWorthwhile = Maff.clamp(Math.min(lifetimeFrames, safety) - distanceFrames, 1.0, 24.0)
    val output          = (1.0 + damage) * valueWorthwhile
    output
  }
}
