package Planning.Plans.Predicates

import Lifecycle.With
import Planning.Composition.UnitMatchers.UnitMatcher
import Planning.{Plan, Yolo}
import ProxyBwapi.Races.{Protoss, Terran}

class SafeToAttack extends Plan {
  override def isComplete: Boolean = {
    if (Yolo.active) return true
    
    if (With.self.isProtoss && With.enemies.forall(_.isTerran)) return pvtSafeToAttack
    if (With.self.isProtoss && With.enemies.forall(_.isZerg))   return pvzSafeToAttack
    
    With.battles.global.globalSafeToAttack
  }
  
  private def countOurs(unitMatcher: UnitMatcher): Int = {
    With.units.ours.count(_.is(unitMatcher))
  }
  
  private def countEnemy(unitMatcher: UnitMatcher): Int = {
    With.units.enemy.count(_.is(unitMatcher))
  }
  
  private def pvtSafeToAttack: Boolean = {
    val vultures  = countEnemy(Terran.Vulture)
    val carriers  = countOurs(Protoss.Carrier)
    val reavers   = countOurs(Protoss.Reaver)
    val dragoons  = countOurs(Protoss.Dragoon)
    val archons   = countOurs(Protoss.Archon)
    val scouts    = countOurs(Protoss.Scout)
    
    val delta     = 4 * carriers + 4 * reavers + 3 * dragoons + 2 * archons + scouts - vultures
    val output    = vultures == 0 || delta > 0
    output
  }
  
  private def pvzSafeToAttack: Boolean = {
    With.battles.global.globalSafeToAttack
    /*
    val plusOneDamge  = With.self.getUpgradeLevel(Protoss.GroundDamage) > With.enemies.map(_.getUpgradeLevel(Zerg.GroundArmor)).max
    val zealotSpeed   = With.self.hasUpgrade(Protoss.ZealotSpeed)
    val zerglingSpeed = With.enemies.exists(_.hasUpgrade(Zerg.ZerglingSpeed))
    val zerglingAspd  = With.enemies.exists(_.hasUpgrade(Zerg.ZerglingAttackSpeed))
    val psionicStorm  = With.self.hasTech(Protoss.PsionicStorm)
    val zerglings     = countEnemy(Zerg.Zergling)
    val hydralisks    = countEnemy(Zerg.Hydralisk)
    val ultralisks    = countEnemy(Zerg.Ultralisk)
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
    
    val airUs         = 3.0 * archons + 3.0 * carriers + Math.pow(corsairs, 1.25) + 1.5 * scouts * 1.0 + dragoons + 1.5 * storms
    val airThem       = mutalisks + scourge / 2
    val safeInAir     = airThem == 0 || airUs > airThem
    
    val groundUs      = 6.0 * archons + 6.0 * carriers + scouts * 1.0 + dragoons + 1.5 * storms
    
    val delta     = 4 * carriers + 4 * reavers + 3 * dragoons + 2 * archons + scouts - vultures
    val output    = delta > 0
    output
    */
  }
  
}