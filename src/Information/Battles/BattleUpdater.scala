package Information.Battles

import Information.Battles.Evaluation.BattleMetrics
import Information.Battles.Simulation.BattleSimulator
import Lifecycle.With
import Mathematics.Pixels.Points

object BattleUpdater {
  
  def run() {
    val allBattles = With.battles.local ++ With.battles.byZone.values :+ With.battles.global
    allBattles.foreach(updateVanguards)
    allBattles.foreach(evaluate)
    With.battles.local.foreach(simulate)
  }
  
  def updateVanguards(battle:Battle) {
    if (battle.happening) {
      battle.groups.foreach(group => {
        val otherGroup = battle.groups.filterNot(_ == group).head
        group.vanguard = group.units.minBy(unit => otherGroup.units.map(unit.pixelDistanceSquared).min).pixelCenter
      })
    }
    else {
      battle.groups.foreach(group => group.vanguard = group.units.headOption.map(_.pixelCenter).getOrElse(Points.middle))
    }
  }
  
  def evaluate(battle:Battle) {
    battle.groups.foreach(group => group.strength = BattleMetrics.estimateStrength(group, battle))
  }
  
  def simulate(battle:Battle) {
    if (battle.happening) {
      battle.simulations = BattleSimulator.simulate(battle)
    }
  }
}
