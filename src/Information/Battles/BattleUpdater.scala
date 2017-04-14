package Information.Battles

import Information.Battles.Types.Battle
import Lifecycle.With
import Mathematics.Pixels.Points

object BattleUpdater {
  
  def run() {
    With.battles.all.foreach(updateBattle)
  }
  
  def updateBattle(battle:Battle) {
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
}
