package Micro.Heuristics.Targeting
import Micro.Decisions.MicroValue
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicVpfOurs extends TargetHeuristic {
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
    val multiplierBase = 240
    val multiplierMiner = if (candidate.gatheringMinerals) 2.0 else 1.0
    val output = multiplierBase * multiplierMiner * valuePerAttack(unit, candidate) / unit.cooldownMaxAgainst(candidate)
    output
  }
  
  def valuePerAttack(shooter: FriendlyUnitInfo, target: UnitInfo): Double = {
    
    val directValueAgainstTarget = MicroValue.valuePerAttack(shooter, target)
    
    if ( ! shooter.unitClass.dealsRadialSplashDamage) {
      return directValueAgainstTarget
    }
    val splashRadius50 = if (target.flying) shooter.unitClass.airSplashRadius50 else shooter.unitClass.groundSplashRadius50
    val splashRadius25 = if (target.flying) shooter.unitClass.airSplashRadius25 else shooter.unitClass.groundSplashRadius25
    
    val bystanders = if (shooter.unitClass.splashesFriendly) target.matchups.allUnits else target.matchups.allies
    val splashed50 = bystanders.filter(bystander => target.flying == bystander.flying && target.pixelDistanceCenter(bystander) <= splashRadius50)
    val splashed25 = bystanders.filter(bystander => target.flying == bystander.flying && target.pixelDistanceCenter(bystander) <= splashRadius25 && target.pixelDistanceCenter(bystander) > splashRadius50)
  
    val splashed50Value = 0.50 * splashed50.map(bystander => MicroValue.valuePerAttack(shooter, bystander)).sum
    val splashed25Value = 0.25 * splashed25.map(bystander => MicroValue.valuePerAttack(shooter, bystander)).sum
    
    val output = directValueAgainstTarget + splashed50Value + splashed25Value
    output
  }
  
}
