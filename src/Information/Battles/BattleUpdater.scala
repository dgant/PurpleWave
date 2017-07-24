package Information.Battles

import Information.Battles.Estimations.{AvatarBuilder, Estimation, Estimator}
import Information.Battles.Types.Battle
import Lifecycle.With
import Mathematics.Points.SpecificPoints
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.EnrichPixel._

object BattleUpdater {
  
  def run() {
    With.battles.all.foreach(updateBattle)
  }
  
  private def updateBattle(battle: Battle) {
    if (battle.happening) {
      battle.teams.foreach(group => {
        val airCentroid = group.units.map(_.pixelCenter).centroid
        val hasGround   = group.units.exists( ! _.flying)
        group.centroid  = group.units.filterNot(_.flying && hasGround).minBy(_.pixelDistanceSquared(airCentroid)).pixelCenter
      })
    }
    
    battle.teams.foreach(group =>
      group.vanguard =
        if (battle.happening)           group.units.minBy(_.pixelDistanceFast(group.opponent.centroid)).pixelCenter
        else if (group.units.nonEmpty)  group.units.minBy(_.pixelDistanceFast(SpecificPoints.middle)).pixelCenter
        else SpecificPoints.middle)
    
    battle
  }
  
  def estimateAvatar(
    battle        : Battle,
    geometric     : Boolean,
    weAttack      : Boolean,
    enemyAttacks  : Boolean,
    weRetreat     : Boolean)
      : Estimation = {
    
    val builder           = new AvatarBuilder
    builder.weAttack      = weAttack
    builder.enemyAttacks  = enemyAttacks
    builder.weRetreat     = weRetreat
    if (geometric) {
      builder.vanguardUs    = Some(battle.us.vanguard)
      builder.vanguardEnemy = Some(battle.enemy.vanguard)
    }
    def fitsAttackCriteria(unit: UnitInfo, mustBeMobile: Boolean): Boolean = ! mustBeMobile || ! unit.unitClass.isBuilding
    battle.us     .units.filter(fitsAttackCriteria(_, weAttack))     .foreach(builder.addUnit)
    battle.enemy  .units.filter(fitsAttackCriteria(_, enemyAttacks)) .foreach(builder.addUnit)
    Estimator.calculate(builder)
  }
  
  def estimateMatchups(battle: Battle): Estimation = {
    Estimator.fromMatchups(battle)
  }
}
