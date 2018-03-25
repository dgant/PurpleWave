package Micro.Heuristics.Targeting
import Lifecycle.With
import Micro.Decisions.MicroValue
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicVpfEnemy extends TargetHeuristic{
  
  val multiplier = 240
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {

    var vpf =
      if ((unit.repairing || unit.constructing) && unit.target.isDefined) {
        unit.target.get.unitClass.repairHpPerFrame * MicroValue.valuePerDamage(unit.target.get)
      }
      else {
        candidate.matchups.vpfDealingDiffused
      }
  
    if (unit.gathering) {
      vpf += With.economy.incomePerFrameMinerals
    }
    
    multiplier * vpf
  }
}
