package Planning.Predicates.Reactive

import Lifecycle.With
import Planning.UnitMatchers.UnitMatcher
import Planning.{Predicate, Yolo}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}

class SafeToMoveOut extends Predicate {
  override def isComplete: Boolean = {
    if (Yolo.active) return true
    
    if (With.self.isProtoss && With.enemies.forall(_.isTerran))   return pvtSafeToAttack
    if (With.self.isProtoss && With.enemies.forall(_.isProtoss))  return pvpSafeToAttack
    if (With.self.isProtoss && With.enemies.forall(_.isZerg))     return pvzSafeToAttack
    
    With.battles.global.globalSafeToAttack
  }
  
  private def countOurs(unitMatcher: UnitMatcher): Int = {
    With.units.countOurs(unitMatcher)
  }
  
  private def countEnemy(unitMatcher: UnitMatcher): Int = {
    With.units.countEnemy(unitMatcher)
  }
  
  private def pvtSafeToAttack: Boolean = {
    val vultures  = countEnemy(Terran.Vulture)
    val carriers  = countOurs(Protoss.Carrier)
    val reavers   = countOurs(Protoss.Reaver)
    val dragoons  = countOurs(Protoss.Dragoon)
    val archons   = countOurs(Protoss.Archon)
    val scouts    = countOurs(Protoss.Scout)
    val zealots   = countOurs(Protoss.Zealot)
    
    val us = (
        4 * carriers
      + 4 * reavers
      + 3 * dragoons
      + 2 * archons
      + 0.2 * zealots
      + scouts
    )
    val delta   = us - vultures * With.blackboard.aggressionRatio()
    val output  = vultures == 0 || delta > 0
    output
  }
  
  private def pvpSafeToAttack: Boolean = {
    val rangeUs       = With.self.hasUpgrade(Protoss.DragoonRange)
    val rangeEnemy    = With.enemies.exists(_.hasUpgrade(Protoss.DragoonRange))
    val speedUs       = With.self.hasUpgrade(Protoss.ZealotSpeed)
    val speedEnemy    = With.enemies.exists(_.hasUpgrade(Protoss.ZealotSpeed))
    
    val zealotsUs     = countOurs(Protoss.Zealot)
    val dragoonsUs    = countOurs(Protoss.Dragoon)
    val reaversUs     = countOurs(Protoss.Reaver)
    val archonsUs     = countOurs(Protoss.Archon)
    val carriersUs    = countOurs(Protoss.Carrier)
    val zealotsEnemy  = countEnemy(Protoss.Zealot)
    val dragoonsEnemy = countEnemy(Protoss.Dragoon)
    val archonsEnemy  = countEnemy(Protoss.Archon)
    val shuttlesEnemy = countEnemy(Protoss.Shuttle)
    
    val scoreDragoon  = 1.0
    val scoreSpeedlot = 1.0
    val scoreReaver   = 2.0
    val scoreShuttle  = 2.0 * scoreReaver
    val scoreArchon   = 2.0
    val scoreCarrier  = 3.0
    
    val scoreUs = (
        dragoonsUs  * (if (rangeUs) scoreDragoon else 0.0)
      + zealotsUs   * (if (speedUs) scoreSpeedlot else 0.0)
      + reaversUs   * scoreReaver
      + archonsUs   * scoreArchon
      + carriersUs  * scoreCarrier
    )
    val scoreEnemy = (
        dragoonsEnemy * (if (rangeEnemy) scoreDragoon else 0.0)
      + zealotsEnemy  * (if (speedEnemy) scoreSpeedlot else 0.0)
      + archonsEnemy  * scoreArchon
      + shuttlesEnemy * scoreShuttle
    )
    val output = scoreEnemy == 0 || scoreEnemy <= scoreUs * With.blackboard.aggressionRatio()
    output 
  }
  
  private def pvzSafeToAttack: Boolean = {
    val plusOneDamage = With.self.getUpgradeLevel(Protoss.GroundDamage) > With.enemies.map(_.getUpgradeLevel(Zerg.GroundArmor)).max
    val zealotSpeed   = With.self.hasUpgrade(Protoss.ZealotSpeed)
    val zerglingSpeed = With.enemies.exists(_.hasUpgrade(Zerg.ZerglingSpeed))
    val zerglingAspd  = With.enemies.exists(_.hasUpgrade(Zerg.ZerglingAttackSpeed))
    val psionicStorm  = With.self.hasTech(Protoss.PsionicStorm)
    val zerglings     = countEnemy(Zerg.Zergling)
    val hydralisks    = countEnemy(Zerg.Hydralisk)
    val ultralisks    = countEnemy(Zerg.Ultralisk)
    val defilers      = countEnemy(Zerg.Defiler)
    val mutalisks     = countEnemy(Zerg.Mutalisk)
    val scourge       = countEnemy(Zerg.Scourge)
    val zealots       = countOurs(Protoss.Zealot)
    val darkTemplar   = countOurs(Protoss.DarkTemplar)
    val dragoons      = countOurs(Protoss.Dragoon)
    val archons       = countOurs(Protoss.Archon)
    val storms        = if (psionicStorm) With.units.ours.map(u => if (u.is(Protoss.HighTemplar)) u.energy / 75 else 0).sum else 0
    val reavers       = countOurs(Protoss.Reaver)
    val corsairs      = countOurs(Protoss.Corsair)
    val scouts        = countOurs(Protoss.Scout)
    val carriers      = countOurs(Protoss.Carrier)
    
    val airUs = (
        3.0 * archons
      + 3.0 * carriers
      + 1.2 * Math.pow(corsairs, 1.2) - Math.max(0.0, corsairs - scourge / 2.0)
      + 1.5 * scouts
      + 1.0 * dragoons
      + 1.5 * storms
    )
    val airThem = mutalisks
    val zealotBonusUs = (
        Math.min(zealots, zerglings / 3) * (if (plusOneDamage) 0.5 else 0.0)
      + Math.min(zealots, hydralisks) * (if (zealotSpeed) 0.5 else 0.0)
    )
    val groundUs = (
        1.0 * archons
      + 6.0 * carriers
      + 1.0 * scouts
      + 1.0 * dragoons
      + 1.0 * (zealots + zealotBonusUs)
      + 1.5 * storms
    )
    val groundThem = (
        1.2 * Math.pow(zerglings, (if (zerglingSpeed) 0.88 else 0.8)) * (if (zerglingAspd) 1.5 else 1.0)
      + 3 * hydralisks
      + 8 * ultralisks
      + 6 * defilers
    )
  
    val safeInAir     = airThem     == 0 || airThem     <= airUs     * With.blackboard.aggressionRatio()
    val safeOnGround  = groundThem  == 0 || groundThem  <= groundUs  * With.blackboard.aggressionRatio()
    val output        = safeInAir && safeOnGround
    output
  }
  
}