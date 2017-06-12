package Information.Battles.Estimation

import Information.Battles.Types.Battle
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class BattleEstimation(
  val battle            : Option[Battle],
  val considerGeometry  : Boolean) {
  
  ///////////
  // Setup //
  ///////////
  
  private val unitsOurs   = new mutable.HashMap[UnitInfo, BattleEstimationUnit]
  private val unitsEnemy  = new mutable.HashMap[UnitInfo, BattleEstimationUnit]
  private val avatarUs    = new BattleEstimationUnit
  private val avatarEnemy = new BattleEstimationUnit
  
  def addUnits(battle: Battle) {
    battle.us.units.foreach(addUnit)
    battle.enemy.units.foreach(addUnit)
  }
  
  def addUnit(unit: UnitInfo) {
    if ( ! eligible(unit)) return
    if (unit.isFriendly) {
      unitsOurs.put(unit, new BattleEstimationUnit(unit, battle, battle.map(_.us), considerGeometry))
      avatarUs.add(unitsOurs(unit))
    }
    else {
      unitsEnemy.put(unit, new BattleEstimationUnit(unit, battle, battle.map(_.enemy), considerGeometry))
      avatarEnemy.add(unitsEnemy(unit))
    }
  }
  
  def removeUnit(unit: UnitInfo) {
    unitsOurs.get(unit).foreach(unitProxy => {
      avatarUs.remove(unitsOurs(unit))
      unitsOurs.remove(unit)
    })
    unitsEnemy.get(unit).foreach(unitProxy => {
      avatarEnemy.remove(unitsEnemy(unit))
      unitsEnemy.remove(unit)
    })
  }
  
  def eligible(unit: UnitInfo):Boolean = {
    if (unit.unitClass.isWorker) return false
    if (unit.unitClass.isBuilding && ! unit.unitClass.helpsInCombat) return false
    if (unit.is(Protoss.Scarab))      return false
    if (unit.is(Protoss.Interceptor)) return false
    unit.aliveAndComplete
  }
  
  /////////////////
  // Calculation //
  /////////////////
  
  var result = new BattleEstimationResult
  
  def recalculate() {
    
    result = new BattleEstimationResult
    
    if (avatarUs.totalUnits == 0 || avatarEnemy.totalUnits == 0) return
    
    result.damageToUs     = dealDamage(avatarEnemy, avatarUs)
    result.damageToEnemy  = dealDamage(avatarUs,    avatarEnemy)
    result.deathsUs       = deaths(avatarUs,        result.damageToUs)
    result.deathsEnemy    = deaths(avatarEnemy,     result.damageToEnemy)
    result.costToUs       = totalCost(avatarUs,     result.damageToUs)
    result.costToEnemy    = totalCost(avatarEnemy,  result.damageToEnemy)
  }
  
  private def dealDamage(from: BattleEstimationUnit, to: BattleEstimationUnit): Double = {
    
    val airFocus        = to.totalFlyers / to.totalUnits
    val groundFocus     = 1.0 - airFocus
    val fromDenominator = from.totalUnits
    val toDenominator   = to.totalUnits
    
    val damagePerFramePerUnit =
      to.vulnerabilityGroundConcussive  / toDenominator * (from.dpfGroundConcussiveFocused + from.dpfGroundConcussiveUnfocused * groundFocus) +
      to.vulnerabilityGroundExplosive   / toDenominator * (from.dpfGroundExplosiveFocused  + from.dpfGroundExplosiveUnfocused  * groundFocus) +
      to.vulnerabilityGroundNormal      / toDenominator * (from.dpfGroundNormalFocused     + from.dpfGroundNormalUnfocused     * groundFocus) +
      to.vulnerabilityAirConcussive     / toDenominator * (from.dpfAirConcussiveFocused    + from.dpfAirConcussiveUnfocused    * airFocus) +
      to.vulnerabilityAirExplosive      / toDenominator * (from.dpfAirExplosiveFocused     + from.dpfAirExplosiveUnfocused     * airFocus) +
      to.vulnerabilityAirNormal         / toDenominator * (from.dpfAirNormalFocused        + from.dpfAirNormalUnfocused        * airFocus)
    
    Math.min(to.totalHealth, damagePerFramePerUnit * With.configuration.battleEstimationFrames / to.totalUnits)
  }
  
  // Examples:
  // 1 unit,   99 damage,  100 hp = 0 deaths
  // 2 units,  199 damage, 200 hp = 1 death
  // 2 units,  99 damage,  200 hp = 0 deaths
  //
  private def deaths(avatar: BattleEstimationUnit, damage: Double):Double = {
    Math.min(avatar.totalUnits, Math.floor(avatar.totalUnits * damage / avatar.totalHealth))
  }
  
  private def totalCost(avatar: BattleEstimationUnit, damage: Double) = {
    avatar.subjectiveValue * damage / avatar.totalHealth
  }
  
  def weWin   : Boolean = result.costToEnemy >  result.costToUs
  def weLose  : Boolean = result.costToEnemy <= result.costToUs
}
