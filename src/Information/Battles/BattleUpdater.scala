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
        // Calculate the centroid.
        // If there's a ground unit, we want to make sure the centroid is over ground.
        val airCentroid = group.units.map(_.pixelCenter).reduce(_.add(_)).divide(group.units.size)
        val hasGround = group.units.exists( ! _.flying)
        group.centroid = group.units.filterNot(_.flying && hasGround).minBy(_.pixelDistanceSquared(airCentroid)).pixelCenter
        group.vanguard = group.units.minBy(unit => group.opponent.units.map(unit.pixelDistanceSquared).min).pixelCenter
      })
    }
    else {
      battle.groups.foreach(group => group.vanguard = group.units.headOption.map(_.pixelCenter).getOrElse(Points.middle))
    }
  }
}
