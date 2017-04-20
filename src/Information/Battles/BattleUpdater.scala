package Information.Battles

import Information.Battles.BattleTypes.Battle
import Lifecycle.With
import Mathematics.Pixels.Points

object BattleUpdater {
  
  def run() {
    With.battles.all.foreach(updateBattle)
  }
  
  def updateBattle(battle:Battle) {
    if (battle.happening) {
      battle.groups.foreach(group => {
        group.vanguard = group.units.minBy(unit => group.opponent.units.map(unit.pixelDistanceSquared).min).pixelCenter
      })
    }
    else {
      battle.groups.foreach(group => group.vanguard = group.units.headOption.map(_.pixelCenter).getOrElse(Points.middle))
    }
  }
}
