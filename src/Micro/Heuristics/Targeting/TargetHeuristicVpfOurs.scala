package Micro.Heuristics.Targeting
import Micro.Decisions.MicroValue
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicVpfOurs extends TargetHeuristic {
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
    val multiplierBase = 240
    val multiplierMiner = if (candidate.gatheringMinerals) 1.5 else 1.0
    val output = multiplierBase * multiplierMiner * valuePerAttack(unit, candidate) / unit.cooldownMaxAgainst(candidate)
    output
  }
  
  def valuePerAttack(shooter: FriendlyUnitInfo, target: UnitInfo): Double = {
    
    val directValueAgainstTarget = MicroValue.valuePerAttackCurrentHp(shooter, target)
    
    if ( ! shooter.unitClass.dealsRadialSplashDamage) {
      return directValueAgainstTarget
    }
    
    def splashValue(bystander: UnitInfo, maxDistance: Double, damageRatio: Double): Double = {
      if (bystander.flying != target.flying) return 0.0
      if (bystander.pixelDistanceCenter(target) > maxDistance) return 0.0
      damageRatio * MicroValue.valuePerAttackCurrentHp(shooter, bystander)
    }
  
    val splashRadius50 = if (target.flying) shooter.unitClass.airSplashRadius50 else shooter.unitClass.groundSplashRadius50
    val splashRadius25 = if (target.flying) shooter.unitClass.airSplashRadius25 else shooter.unitClass.groundSplashRadius25
    val bystanders = if (shooter.unitClass.splashesFriendly) target.matchups.allUnits else target.matchups.allies
    var splashed50Value = 0.0
    var splashed25Value = 0.0
    bystanders.foreach(bystander => splashed50Value += splashValue(bystander, splashRadius50, 0.5))
    bystanders.foreach(bystander => splashed25Value += splashValue(bystander, splashRadius25, 0.25))
    
    val output = directValueAgainstTarget + splashed50Value + splashed25Value
    output
  }
  
}
