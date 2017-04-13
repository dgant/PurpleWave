package Information.Battles

import Information.Battles.Evaluation.BattleMetrics
import Information.Battles.Simulation.BattleSimulator
import Mathematics.Pixels.Points

object BattleUpdater {
  
  def assess(battles:Iterable[Battle]) {
    battles.foreach(update)
  }
  
  def update(battle:Battle) {
    if (battle.happening) {
      battle.groups.foreach(group => {
        val otherGroup = battle.groups.filterNot(_ == group).head
        group.vanguard = group.units.minBy(unit => otherGroup.units.map(unit.pixelDistanceSquared).min).pixelCenter
      })
  
      battle.simulations = BattleSimulator.simulate(battle)
    }
    else {
      battle.groups.foreach(group => group.vanguard = group.units.headOption.map(_.pixelCenter).getOrElse(Points.middle))
    }
    battle.groups.foreach(group => group.strength = BattleMetrics.estimateStrength(group, battle))
  }
}
