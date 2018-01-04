package Information.Battles

import Information.Battles.Types.Battle
import Lifecycle.With
import Mathematics.Points.SpecificPoints
import Utilities.ByOption
import Utilities.EnrichPixel._

object BattleUpdater {
  
  def run() {
    (With.battles.local :+ With.battles.global).foreach(updateBattle)
  }
  
  private def updateBattle(battle: Battle) {
    battle.teams.foreach(group => {
      val airCentroid = group.units.map(_.pixelCenter).centroid
      val hasGround   = group.units.exists( ! _.flying)
      group.centroid  = group.units.filterNot(_.flying && hasGround).minBy(_.pixelDistanceSquared(airCentroid)).pixelCenter
    })
    
    battle.teams.foreach(group =>
      group.vanguard = ByOption
        .minBy(group.units)(_.pixelDistanceFast(group.opponent.centroid))
        .map(_.pixelCenter)
        .getOrElse(SpecificPoints.middle))
  }
}
