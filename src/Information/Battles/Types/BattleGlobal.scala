package Information.Battles.Types

import Information.Battles.Prediction.Estimation.{AvatarBuilder, EstimateAvatar}
import Information.Battles.Prediction.Prediction
import Lifecycle.With
import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.UnitInfo

class BattleGlobal(us: Team, enemy: Team) extends Battle(us, enemy) {
  
  lazy val estimationAbstract           : Prediction  = estimateAvatar(this, geometric = false, weAttack = true,  enemyAttacks = true,  weRetreat = false)
  lazy val estimationAbstractOffense    : Prediction  = estimateAvatar(this, geometric = false, weAttack = true,  enemyAttacks = false, weRetreat = false)
  lazy val estimationAbstractDefense    : Prediction  = estimateAvatar(this, geometric = false, weAttack = false, enemyAttacks = true,  weRetreat = false)
  
  lazy val globalSafeToAttack: Boolean = globalSafe(estimationAbstractOffense, With.blackboard.aggressionRatio)
  lazy val globalSafeToDefend: Boolean = globalSafe(estimationAbstractDefense, With.blackboard.safetyRatio) || globalSafeToAttack
  
  def sumValue(units: Iterable[UnitInfo]): Double = units.toVector.map(_.subjectiveValue).sum
  lazy val valueEnemyArmy   : Double = sumValue(With.units.enemy.filter(u => u.unitClass.dealsDamage))
  lazy val valueUsArmy      : Double = sumValue(With.units.ours.filter(u => u.unitClass.dealsDamage))
  lazy val valueRatioTarget : Double = Math.min(With.configuration.battleValueTarget, PurpleMath.nanToOne(valueEnemyArmy / (valueEnemyArmy + valueUsArmy)))
  
  private def globalSafe(estimation: Prediction, discountFactor: Double): Boolean = {
    val tradesEffectively = discountFactor * estimation.costToEnemy - estimation.costToUs >= 0
    val killsEffectively =
      estimation.enemyDies &&
      estimation.weSurvive &&
      estimation.costToUs < estimationAbstractOffense.avatarUs.subjectiveValue
    
    (estimation.totalUnitsUs > 0 || estimation.totalUnitsEnemy == 0) &&
    (tradesEffectively || killsEffectively)
  }
  
  private def estimateAvatar(
    battle        : Battle,
    geometric     : Boolean,
    weAttack      : Boolean,
    enemyAttacks  : Boolean,
    weRetreat     : Boolean)
      : Prediction = {
    
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
