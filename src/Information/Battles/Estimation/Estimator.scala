package Information.Battles.Estimation

import Information.Battles.Types.{Battle, Team}
import Lifecycle.With
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class Estimator(val considerGeometry: Boolean) {
  
  private var battle: Option[Battle] = None
  
  def this(argBattle: Battle, considerGeometry: Boolean) {
    this(considerGeometry)
    battle = Some(argBattle)
    argBattle.groups.flatMap(_.units).foreach(addUnit)
  }
  
  ///////////
  // Setup //
  ///////////
  
  private val unitsOurs   = new mutable.HashMap[UnitInfo, Avatar]
  private val unitsEnemy  = new mutable.HashMap[UnitInfo, Avatar]
  private val avatarUs    = new Avatar
  private val avatarEnemy = new Avatar
  
  def addUnit(unit: UnitInfo) {
    if ( ! eligible(unit)) return
    if (unit.isFriendly)  addUnit(unit, avatarUs,     unitsOurs,  battle.map(_.us))
    else                  addUnit(unit, avatarEnemy,  unitsEnemy, battle.map(_.enemy))
  }
  
  def removeUnit(unit: UnitInfo) {
    removeUnit(unit, avatarUs,    unitsOurs)
    removeUnit(unit, avatarEnemy, unitsEnemy)
  }
  
  private def addUnit(
    unit      : UnitInfo,
    bigAvatar : Avatar,
    avatars   : mutable.HashMap[UnitInfo, Avatar],
    group     : Option[Team]) {
    
    invalidateResult()
    val newAvatar = new Avatar(unit, group, considerGeometry)
    avatars.put(unit, newAvatar)
    bigAvatar.add(newAvatar)
  }
  
  private def removeUnit(
    unit      : UnitInfo,
    bigAvatar : Avatar,
    avatars   : mutable.HashMap[UnitInfo, Avatar]) {
  
    invalidateResult()
    avatars
      .get(unit)
      .foreach(avatar => {
        bigAvatar.remove(avatar)
        avatars.remove(unit)
      })
  }
  
  private def eligible(unit: UnitInfo): Boolean = {
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
  
  def result: Estimation = {
    validResult = validResult.orElse(Some(recalculate))
    validResult.get
  }
  
  def weGainValue : Boolean = result.costToEnemy  >   result.costToUs
  def weLoseValue : Boolean = result.costToEnemy  <=  result.costToUs
  def weSurvive   : Boolean = result.deathsUs     <   unitsOurs.size
  def weDie       : Boolean = result.deathsUs     >=  unitsOurs.size
  
  private var validResult: Option[Estimation] = None
  private def invalidateResult() {
    validResult = None
  }
  
  private def recalculate: Estimation = {
    
    val output = new Estimation
    
    if (avatarUs.totalUnits <= 0 || avatarEnemy.totalUnits <= 0) return output
    
    output.damageToUs     = dealDamage  (avatarEnemy, avatarUs)
    output.damageToEnemy  = dealDamage  (avatarUs,    avatarEnemy)
    output.deathsUs       = deaths      (avatarUs,    output.damageToUs)
    output.deathsEnemy    = deaths      (avatarEnemy, output.damageToEnemy)
    output.costToUs       = totalCost   (avatarUs,    output.damageToUs)
    output.costToEnemy    = totalCost   (avatarEnemy, output.damageToEnemy)
  
    output
  }
  
  private def dealDamage(from: Avatar, to: Avatar): Double = {
    
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
  private def deaths(avatar: Avatar, damage: Double): Double = {
    Math.min(avatar.totalUnits, Math.floor(avatar.totalUnits * damage / avatar.totalHealth))
  }
  
  private def totalCost(avatar: Avatar, damage: Double) = {
    avatar.subjectiveValue * damage / avatar.totalHealth
  }
}
