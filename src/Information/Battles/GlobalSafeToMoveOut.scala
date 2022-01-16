package Information.Battles

import Lifecycle.With
import Planning.Predicates.Strategy.EnemyRecentStrategy
import Planning.UnitMatchers.{MatchAnd, MatchComplete, UnitMatcher}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Strategery.Strategies.Zerg.ZvZ9PoolSpeed

object GlobalSafeToMoveOut {
  def apply(): Boolean = {
    val output =
      if (With.yolo.active) true
      else if (With.self.isProtoss  && With.enemies.forall(_.isTerran))   pvtSafeToAttack
      else if (With.self.isProtoss  && With.enemies.forall(_.isProtoss))  pvpSafeToAttack
      else if (With.self.isProtoss  && With.enemies.forall(_.isZerg))     pvzSafeToAttack
      else if (With.self.isZerg     && With.enemies.forall(_.isZerg))     zvzSafeToAttack
      else With.battles.global.globalSafeToAttack
    output
  }
  
  private def countOurs(unitMatcher: UnitMatcher): Int = {
    With.units.countOurs(MatchAnd(unitMatcher, MatchComplete))
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
    val rangeUs         = With.self.hasUpgrade(Protoss.DragoonRange)
    val rangeEnemy      = With.enemies.exists(_.hasUpgrade(Protoss.DragoonRange))
    val speedUs         = With.self.hasUpgrade(Protoss.ZealotSpeed)
    val speedEnemy      = With.enemies.exists(_.hasUpgrade(Protoss.ZealotSpeed))

    val zealotsUs       = countOurs(Protoss.Zealot)
    val dragoonsUs      = countOurs(Protoss.Dragoon)
    val archonsUs       = countOurs(Protoss.Archon)
    val carriersUs      = countOurs(Protoss.Carrier)
    val reaversUs       = countOurs(Protoss.Reaver)
    val shuttlesUs      = countOurs(Protoss.Shuttle)
    val zealotsEnemy    = countEnemy(Protoss.Zealot)
    var dragoonsEnemy   = countEnemy(Protoss.Dragoon)
    val archonsEnemy    = countEnemy(Protoss.Archon)
    val carriersEnemy   = countEnemy(Protoss.Carrier)
    val reaversEnemy    = countEnemy(Protoss.Reaver)
    val shuttlesEnemy   = countEnemy(Protoss.Shuttle)
    
    val scoreDragoon  = 1.0
    val scoreSpeedlot = 1.0
    val scoreTRexArms = 0.5
    val scoreSlowlot  = 0.5
    val scoreArchon   = 2.0
    val scoreCarrier  = 3.0
    val scoreReaver   = 2.0
    val scoreShuttle  = 1.25
    val scoreStorm    = 2.0

    val scoreUs = (
        dragoonsUs  * (if (rangeUs || ! rangeEnemy) scoreDragoon else scoreTRexArms)
      + zealotsUs   * (if (speedUs) scoreSpeedlot else scoreSlowlot)
      + archonsUs   * scoreArchon
      + carriersUs  * scoreCarrier
      + reaversUs   * scoreReaver
      + Math.min(shuttlesUs, 2 * reaversUs) * scoreShuttle
      + scoreStorm * storms)

    val scoreEnemy = (
        dragoonsEnemy * (if (rangeEnemy || ! rangeUs) scoreDragoon else scoreTRexArms)
      + zealotsEnemy  * (if (speedEnemy) scoreSpeedlot else scoreSlowlot)
      + archonsEnemy  * scoreArchon
      + carriersEnemy * scoreCarrier
      + reaversEnemy * scoreReaver
      + Math.min(shuttlesEnemy, 2 * reaversEnemy) * scoreShuttle
    )
    val output = scoreEnemy == 0 || scoreEnemy < scoreUs * With.blackboard.aggressionRatio()
    output 
  }
  
  private def pvzSafeToAttack: Boolean = {
    val plusOneDamage = With.self.getUpgradeLevel(Protoss.GroundDamage) > With.enemies.map(_.getUpgradeLevel(Zerg.GroundArmor)).max
    val zealotSpeed   = With.self.hasUpgrade(Protoss.ZealotSpeed)
    val zerglingSpeed = With.enemies.exists(_.hasUpgrade(Zerg.ZerglingSpeed))
    val zerglingAspd  = With.enemies.exists(_.hasUpgrade(Zerg.ZerglingAttackSpeed))
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
    val reavers       = countOurs(Protoss.Reaver)
    val shuttles      = countOurs(Protoss.Shuttle)
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
      + 1.5 * dragoons
      + 1.0 * (zealots + zealotBonusUs)
      + 1.5 * storms
      + 4.5 * reavers
      + 2.0 * Math.min(reavers / 2.0, shuttles)
    )
    val groundThem = (
        0.6  * Math.pow(zerglings, (if (zerglingSpeed) 0.88 else 0.8)) * (if (zerglingAspd) 1.5 else 1.0)
      + 1.25 * hydralisks
      + 8.0  * ultralisks
      + 6.0  * defilers
    )
  
    val safeInAir     = airThem     == 0 || airThem     <= airUs     * With.blackboard.aggressionRatio()
    val safeOnGround  = groundThem  == 0 || groundThem  <= groundUs  * With.blackboard.aggressionRatio()
    val output        = safeInAir && safeOnGround
    output
  }

  def zvzSafeToAttack: Boolean = {
    val speedUs = With.self.hasUpgrade(Zerg.ZerglingSpeed)
    val speedEnemy = With.enemy.hasUpgrade(Zerg.ZerglingSpeed)
    val speedExpectedFirst = (
      ZvZ9PoolSpeed.activate
      || ( ! With.fingerprints.ninePool.matches && ! With.fingerprints.overpool.matches && new EnemyRecentStrategy(With.fingerprints.twelveHatch).apply)
      || With.fingerprints.twelveHatch.matches
      || With.fingerprints.twelvePool.matches)
    val okayOnSpeed = speedUs || (speedExpectedFirst && ! speedEnemy)

    val okayOnZerglings = okayOnSpeed && (
      countOurs(Zerg.Zergling) >= countEnemy(Zerg.Zergling)
      || (countOurs(Zerg.Mutalisk) > countEnemy(Zerg.Mutalisk)))

    val okayOnMutalisks = countOurs(Zerg.Mutalisk) + countOurs(Zerg.Scourge) >= countEnemy(Zerg.Mutalisk)
    val output = okayOnZerglings && okayOnMutalisks
    output
  }

  private def storms: Int = if (With.self.hasTech(Protoss.PsionicStorm)) With.units.ours.map(u => if (u.is(Protoss.HighTemplar)) u.energy / 75 else 0).sum else 0
  
}