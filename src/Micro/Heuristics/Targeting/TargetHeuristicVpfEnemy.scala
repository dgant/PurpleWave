package Micro.Heuristics.Targeting
import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Micro.Decisions.MicroValue
import Planning.Composition.UnitMatchers.{UnitMatchAnd, UnitMatchComplete, UnitMatchNot, UnitMatchOr}
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetHeuristicVpfEnemy extends TargetHeuristic{
  
  val multiplier = 240
  
  override def evaluate(unit: FriendlyUnitInfo, candidate: UnitInfo): Double = {
    var vpf =
      if ((unit.repairing || unit.constructing) && unit.target.isDefined) {
        unit.target.get.unitClass.repairHpPerFrame * MicroValue.valuePerDamage(unit.target.get)
      }
      else if (unit.isAny(
        Protoss.ShieldBattery,
        Zerg.CreepColony,
        UnitMatchAnd(
          UnitMatchNot(UnitMatchComplete),
          UnitMatchOr(
            Zerg.SunkenColony,
            Zerg.SporeColony)))) {
        
        // Hack math just to get this target into consideration
        Protoss.Dragoon.subjectiveValue.toDouble / GameTime(0, 10)()
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
