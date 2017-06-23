package Information.Battles.Estimation

import Mathematics.Points.Pixel
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class AvatarBuilder {
  
  var vanguardUs    : Option[Pixel] = None
  var vanguardEnemy : Option[Pixel] = None
  val unitsOurs   = new mutable.HashMap[UnitInfo, Avatar]
  val unitsEnemy  = new mutable.HashMap[UnitInfo, Avatar]
  val avatarUs    = new Avatar
  val avatarEnemy = new Avatar

  ///////////
  // Setup //
  ///////////
  
  def addUnit(unit: UnitInfo) {
    if ( ! eligible(unit)) return
    if (unit.isFriendly)  addUnit(unit, avatarUs,     unitsOurs,  vanguardEnemy)
    else                  addUnit(unit, avatarEnemy,  unitsEnemy, vanguardUs)
  }
  
  def removeUnit(unit: UnitInfo) {
    removeUnit(unit, avatarUs,    unitsOurs)
    removeUnit(unit, avatarEnemy, unitsEnemy)
  }
  
  private def addUnit(
    unit          : UnitInfo,
    bigAvatar     : Avatar,
    avatars       : mutable.HashMap[UnitInfo, Avatar],
    enemyVanguard : Option[Pixel]) {
    
    invalidateResult()
    val newAvatar = new Avatar(unit, enemyVanguard)
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
    if (unit.unitClass.isWorker   && ! unit.isBeingViolent)           return false
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
    estimation = estimation.orElse(Some(Estimator.calculate(this)))
    estimation.get
  }
  
  private var estimation: Option[Estimation] = None
  private def invalidateResult() {
    estimation = None
  }
}
