package Information.Battles.Types

import Information.Battles.Estimations.{AvatarBuilder, EstimateAvatar, Estimation}
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

class BattleGlobal(us: Team, enemy: Team) extends Battle(us, enemy) {
  
  lazy val estimationAbstract           : Estimation  = estimateAvatar(this, geometric = false, weAttack = true,  enemyAttacks = true,  weRetreat = false)
  lazy val estimationAbstractOffense    : Estimation  = estimateAvatar(this, geometric = false, weAttack = true,  enemyAttacks = false, weRetreat = false)
  lazy val estimationAbstractDefense    : Estimation  = estimateAvatar(this, geometric = false, weAttack = false, enemyAttacks = true,  weRetreat = false)
  
  lazy val globalSafeToAttack: Boolean = globalSafe(estimationAbstractOffense, With.blackboard.aggressionRatio)
  lazy val globalSafeToDefend: Boolean = globalSafe(estimationAbstractDefense, With.blackboard.safetyRatio) || globalSafeToAttack
  
  private def globalSafe(estimation: Estimation, discountFactor: Double): Boolean = {
    val tradesEffectively = discountFactor * estimation.costToEnemy - estimation.costToUs >= 0
    val killsEffectively  =
      estimation.enemyDies &&
        estimation.weSurvive &&
        estimation.costToUs < estimationAbstractOffense.avatarUs.subjectiveValue
    
    tradesEffectively || killsEffectively
  }
  
  private def estimateAvatar(
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
    EstimateAvatar.calculate(builder)
  }
}
