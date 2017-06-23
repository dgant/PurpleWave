package Information.Battles.Estimation

import Information.Battles.Types.{Battle, Team}
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class Estimator(val considerGeometry: Boolean) {
  
  private var battle: Option[Battle] = None
  
  def this(argBattle: Battle, considerGeometry: Boolean) {
    this(considerGeometry)
    battle = Some(argBattle)
    argBattle.teams.flatMap(_.units).foreach(addUnit)
  }
  
  ///////////
  // Setup //
  ///////////
  
  val unitsOurs   = new mutable.HashMap[UnitInfo, Avatar]
  val unitsEnemy  = new mutable.HashMap[UnitInfo, Avatar]
  val avatarUs    = new Avatar
  val avatarEnemy = new Avatar
  
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
  
  /////////////
  // Results //
  /////////////

  def result: Estimation = {
    validResult = validResult.orElse(Some(EstimationCalculator.calculate(this)))
    validResult.get
  }
  
  private var validResult: Option[Estimation] = None
  private def invalidateResult() {
    validResult = None
  }
  
  def weGainValue : Boolean = result.costToEnemy  >   result.costToUs
  def weLoseValue : Boolean = result.costToEnemy  <   result.costToUs
  def weSurvive   : Boolean = result.deathsUs     <   unitsOurs.size
  def weDie       : Boolean = result.deathsUs     >=  unitsOurs.size
}
