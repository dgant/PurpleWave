package Information.Battles

import Information.Battles.Evaluation.BattleMetrics
import Information.Battles.Simulation.BattleSimulator
import Mathematics.Positions.Positions

object BattleUpdater {
  
  def assess(battles:Iterable[Battle]) {
    battles.foreach(update)
  }
  
  def update(battle:Battle) {
    if (battle.happening) {
      battle.groups.foreach(group => {
        val otherGroup = battle.groups.filterNot(_ == group).head
        group.vanguard = group.units.minBy(unit => otherGroup.units.map(unit.pixelDistance).min).pixelCenter
      })
  
      battle.simulations = BattleSimulator.simulate(battle)
    }
    else {
      battle.groups.foreach(group => group.vanguard = group.units.headOption.map(_.pixelCenter).getOrElse(Positions.middle))
    }
    battle.groups.foreach(group => group.strength = BattleMetrics.estimateStrength(group, battle))
  }
}
