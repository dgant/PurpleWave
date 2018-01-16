package Micro.Actions.Combat.Spells

import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object Heal extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Terran.Medic)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val targets = validTargets(unit)
    val target  = ByOption.minBy(targets)(_.pixelDistanceFast(unit))
    
    target.foreach(someTarget => {
      With.commander.attackMove(unit, someTarget.pixelCenter)
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
    val distancePixels  = medic.pixelDistanceFast(patient)
    val distanceFrames  = PurpleMath.nanToInfinity(distancePixels / medic.topSpeed)
    val lifetimeFrames  = patient.matchups.framesToLiveDiffused
    val damage          = patient.unitClass.maxHitPoints - patient.hitPoints
    val safety          = PurpleMath.signum(Math.max(0.0, patient.matchups.framesOfSafetyDiffused))
    val receiving       = PurpleMath.signum(patient.matchups.vpfReceivingDiffused)
    val dealing         = PurpleMath.signum(patient.matchups.vpfDealingDiffused)
    val valueWorthwhile = PurpleMath.clamp(Math.min(lifetimeFrames, safety) - distanceFrames, 1.0, 24.0)
    val valueTrading    = (1.0 + receiving) * (1.0 + dealing)
    val output          = (1.0 + damage) * valueWorthwhile * valueTrading
    output
  }
}
