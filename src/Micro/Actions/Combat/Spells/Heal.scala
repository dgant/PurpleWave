package Micro.Actions.Combat.Spells

import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object Heal extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.is(Terran.Medic)
    && (unit.matchups.battle.isDefined || unit.agent.toForm.isEmpty)
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val targets = validTargets(unit)
    val target  = ByOption.minBy(targets)(patient =>
      if (unit.matchups.battle.isDefined && unit.matchups.threatsViolent.nonEmpty)
        patient.pixelDistanceEdge(unit)
      else
        patient.pixelDistanceCenter(unit.agent.destination))
    
    target.foreach(someTarget => {
      val targetPixel = unit.pixelCenter.project(someTarget.pixelCenter, unit.pixelDistanceEdge(someTarget) - 16.0)
      if (someTarget.pixelDistanceCenter(unit) < 96.0) {
        With.commander.attackMove(unit, targetPixel)
      }
    })
  }
  
  private def validTargets(unit: FriendlyUnitInfo): Vector[UnitInfo] = {
    unit.teammates
      .toVector
      .filter(u =>
        u.unitClass.isOrganic
        && ! u.beingHealed
        && ! u.is(Terran.Medic)
        && (u.hitPoints < u.unitClass.maxHitPoints|| u.matchups.threats.nonEmpty))
  }
  
  private def targetValue(medic: FriendlyUnitInfo, patient: UnitInfo): Double = {
    val distancePixels  = medic.pixelDistanceEdge(patient)
    val distanceFrames  = PurpleMath.nanToInfinity(distancePixels / medic.topSpeed)
    val lifetimeFrames  = patient.matchups.framesToLive
    val damage          = patient.unitClass.maxHitPoints - patient.hitPoints
    val safety          = PurpleMath.signum(Math.max(0.0, patient.matchups.framesOfSafety))
    val receiving       = PurpleMath.signum(patient.matchups.vpfReceiving)
    val dealing         = PurpleMath.signum(patient.matchups.vpfDealingInRange)
    val valueWorthwhile = PurpleMath.clamp(Math.min(lifetimeFrames, safety) - distanceFrames, 1.0, 24.0)
    val valueTrading    = (1.0 + receiving) * (1.0 + dealing)
    val output          = (1.0 + damage) * valueWorthwhile * valueTrading
    output
  }
}
