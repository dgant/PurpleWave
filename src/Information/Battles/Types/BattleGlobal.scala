package Information.Battles.Types

import Information.Battles.Prediction.Estimation.{AvatarBuilder, EstimateAvatar}
import Information.Battles.Prediction.PredictionGlobal
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

class BattleGlobal(us: Team, enemy: Team) extends Battle(us, enemy) {

  private lazy val estimationAbstractOffense: PredictionGlobal = estimateAvatar(this, weAttack = true)
  private lazy val estimationAbstractDefense: PredictionGlobal = estimateAvatar(this, weAttack = false)
  
  lazy val globalSafeToAttack: Boolean = globalSafe(estimationAbstractOffense, With.blackboard.aggressionRatio())
  lazy val globalSafeToDefend: Boolean = globalSafe(estimationAbstractDefense, 1.2) || globalSafeToAttack
  
  private def globalSafe(estimation: PredictionGlobal, discountFactor: Double): Boolean = {
    val tradesEffectively = discountFactor * estimation.costToEnemy - estimation.costToUs >= 0
    val killsEffectively =
      estimation.enemyDies &&
      estimation.weSurvive &&
      estimation.costToUs < estimationAbstractOffense.avatarUs.subjectiveValue
    
    (estimation.totalUnitsUs > 0 || estimation.totalUnitsEnemy == 0) &&
    (tradesEffectively || killsEffectively)
  }

  private def fitsAttackCriteria(unit: UnitInfo, mustBeMobile: Boolean): Boolean = (
    (! mustBeMobile || ! unit.unitClass.isBuilding)
    && ! unit.unitClass.isWorker
    && unit.canAttack)
  
  private def estimateAvatar(battle: Battle, weAttack: Boolean): PredictionGlobal = {
    val builder           = new AvatarBuilder
    builder.weAttack      = weAttack
    builder.enemyAttacks  = ! weAttack

    battle.us     .units.view.filter(fitsAttackCriteria(_, builder.weAttack))     .foreach(builder.addUnit)
    battle.enemy  .units.view.filter(fitsAttackCriteria(_, builder.enemyAttacks)) .foreach(builder.addUnit)
    EstimateAvatar.calculate(builder)
  }
}
