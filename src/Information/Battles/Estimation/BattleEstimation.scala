package Information.Battles.Estimation

import Information.Battles.Types.{Battle, BattleGroup}
import Lifecycle.With
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class BattleEstimation(
  val battle            : Option[Battle],
  val considerGeometry  : Boolean) {
  
  ///////////
  // Setup //
  ///////////
  
  private val unitsOurs   = new mutable.HashMap[UnitInfo, BattleAvatar]
  private val unitsEnemy  = new mutable.HashMap[UnitInfo, BattleAvatar]
  private val avatarUs    = new BattleAvatar
  private val avatarEnemy = new BattleAvatar
  
  def addUnits(battle: Battle) {
    battle.groups.flatMap(_.units).foreach(addUnit)
  }
  
  def addUnit(unit: UnitInfo) {
    if ( ! eligible(unit)) return
    if (unit.isFriendly)  addUnit(unit, avatarUs,     unitsOurs,  battle.map(_.us))
    else                  addUnit(unit, avatarEnemy,  unitsEnemy, battle.map(_.us))
  }
  
  def removeUnit(unit: UnitInfo) {
    removeUnit(unit, avatarUs,    unitsOurs)
    removeUnit(unit, avatarEnemy, unitsEnemy)
  }
  
  private def addUnit(
    unit      : UnitInfo,
    bigAvatar : BattleAvatar,
    avatars   : mutable.HashMap[UnitInfo, BattleAvatar],
    group     : Option[BattleGroup]) {
    
    val newAvatar = new BattleAvatar(unit, group, considerGeometry)
    avatars.put(unit, newAvatar)
    bigAvatar.add(newAvatar)
  }
  
  private def removeUnit(
    unit      : UnitInfo,
    bigAvatar : BattleAvatar,
    avatars   : mutable.HashMap[UnitInfo, BattleAvatar]) {
    
    avatars
      .get(unit)
      .foreach(avatar => {
        bigAvatar.remove(avatar)
        avatars.remove(unit)
      })
  }
  
  def eligible(unit: UnitInfo): Boolean = {
    if (unit.unitClass.isWorker && ! unit.isBeingViolent)             return false
    if (unit.unitClass.isBuilding && ! unit.unitClass.helpsInCombat)  return false
    if (unit.is(Terran.SpiderMine))                                   return false
    if (unit.is(Protoss.Scarab))                                      return false
    if (unit.is(Protoss.Interceptor))                                 return false
    
    unit.aliveAndComplete
  }
  
  /////////////////
  // Calculation //
  /////////////////
  
  var result = new BattleEstimationResult
  
  def recalculate() {
    
    result = new BattleEstimationResult
    
    if (avatarUs.totalUnits <= 0 || avatarEnemy.totalUnits <= 0) return
    
    result.damageToUs     = dealDamage  (avatarEnemy, avatarUs)
    result.damageToEnemy  = dealDamage  (avatarUs,    avatarEnemy)
    result.deathsUs       = deaths      (avatarUs,    result.damageToUs)
    result.deathsEnemy    = deaths      (avatarEnemy, result.damageToEnemy)
    result.costToUs       = totalCost   (avatarUs,    result.damageToUs)
    result.costToEnemy    = totalCost   (avatarEnemy, result.damageToEnemy)
  }
  
  private def dealDamage(from: BattleAvatar, to: BattleAvatar): Double = {
    
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
  private def deaths(avatar: BattleAvatar, damage: Double): Double = {
    Math.min(avatar.totalUnits, Math.floor(avatar.totalUnits * damage / avatar.totalHealth))
  }
  
  private def totalCost(avatar: BattleAvatar, damage: Double) = {
    avatar.subjectiveValue * damage / avatar.totalHealth
  }
  
  def weGainValue : Boolean = result.costToEnemy  >   result.costToUs
  def weLoseValue : Boolean = result.costToEnemy  <=  result.costToUs
  def weSurvive   : Boolean = result.deathsUs     <   unitsOurs.size
  def weDie       : Boolean = result.deathsUs     >=  unitsOurs.size
}
