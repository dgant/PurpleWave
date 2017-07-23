package Information.Battles.Estimations

import Mathematics.Points.Pixel
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class AvatarBuilder {
  
  var vanguardUs    : Option[Pixel] = None
  var vanguardEnemy : Option[Pixel] = None
  var weAttack      : Boolean       = false
  var enemyAttacks  : Boolean       = false
  var weRetreat     : Boolean       = false
  var enemyRetreats : Boolean       = false
  val unitsOurs   = new mutable.HashMap[UnitInfo, Avatar]
  val unitsEnemy  = new mutable.HashMap[UnitInfo, Avatar]
  var avatarUs    = new Avatar
  var avatarEnemy = new Avatar
  
  def addUnit(unit: UnitInfo) {
    if ( ! eligible(unit)) return
    if (unit.isFriendly)  addUnit(unit, avatarUs,     unitsOurs,  vanguardEnemy,  weAttack,     weRetreat,      enemyRetreats)
    else                  addUnit(unit, avatarEnemy,  unitsEnemy, vanguardUs,     enemyAttacks, enemyRetreats,  weRetreat)
  }
  
  def removeUnit(unit: UnitInfo) {
    removeUnit(unit, avatarUs,    unitsOurs)
    removeUnit(unit, avatarEnemy, unitsEnemy)
  }
  
  private def addUnit(
    unit              : UnitInfo,
    bigAvatar         : Avatar,
    avatars           : mutable.HashMap[UnitInfo, Avatar],
    opposingVanguard  : Option[Pixel],
    attacking         : Boolean,
    retreating        : Boolean,
    chasing           : Boolean) {
    
    if (avatars.contains(unit)) return
    
    val newAvatar = new Avatar(unit, opposingVanguard, attacking, retreating, chasing)
    avatars.put(unit, newAvatar)
    bigAvatar.add(newAvatar)
  }
  
  private def removeUnit(
    unit      : UnitInfo,
    bigAvatar : Avatar,
    avatars   : mutable.HashMap[UnitInfo, Avatar]) {
  
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
}
