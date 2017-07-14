package Information.Battles

import Information.Battles.Estimation.{AvatarBuilder, Estimator}
import Information.Battles.Types.Battle
import Lifecycle.With
import Mathematics.Points.SpecificPoints

object BattleUpdater {
  
  def run() {
    With.battles.all.foreach(updateBattle)
  }
  
  private def updateBattle(battle: Battle) {
    
    if (battle.happening) {
      battle.teams.foreach(group => {
        val airCentroid = group.units.map(_.pixelCenter).reduce(_.add(_)).divide(group.units.size)
        val hasGround   = group.units.exists( ! _.flying)
        group.centroid  = group.units.filterNot(_.flying && hasGround).minBy(_.pixelDistanceSquared(airCentroid)).pixelCenter
      })
    }
    
    battle.teams.foreach(group =>
      group.vanguard =
        if (battle.happening) group.units.minBy(_.pixelDistanceFast(group.opponent.centroid)).pixelCenter
        else                  group.units.headOption.map(_.pixelCenter).getOrElse(SpecificPoints.middle))
  
    val avatarBuilderAbstract   = new AvatarBuilder
    val avatarBuilderGeometric  = new AvatarBuilder
    avatarBuilderGeometric.vanguardUs     = Some(battle.us.vanguard)
    avatarBuilderGeometric.vanguardEnemy  = Some(battle.enemy.vanguard)
    battle.teams.flatMap(_.units).foreach(avatarBuilderAbstract.addUnit)
    battle.teams.flatMap(_.units).foreach(avatarBuilderGeometric.addUnit)
    battle.estimationAbstract   = Estimator.calculate(avatarBuilderAbstract)
    battle.estimationGeometric  = Estimator.calculate(avatarBuilderGeometric)
  }
}
