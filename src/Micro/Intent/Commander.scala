package Micro.Intent

import Lifecycle.With
import Mathematics.Pixels.{Pixel, Tile}
import ProxyBwapi.Races.Protoss
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.Upgrades.Upgrade
import Utilities.CountMap

// Commander is responsible for issuing unit commands
// in a way that Brood War handles gracefully.
//
// The goal is for the rest of the code base to be blissfully unaware
// of Brood War's glitchy unit behavior.
//
class Commander {
  
  private val nextOrderFrame = new CountMap[FriendlyUnitInfo]
  
  def run() {
    nextOrderFrame.keySet.filterNot(_.alive).foreach(nextOrderFrame.remove)
  }
  
  def eligibleForResleeping(unit: FriendlyUnitInfo):Boolean = {
    nextOrderFrame.contains(unit) && (unit.attackAnimationHappening || unit.attackStarting)
  }
  
  def readyForCommand(unit: FriendlyUnitInfo):Boolean = {
    nextOrderFrame(unit) < With.frame
  }
  
  def attack(intent:Intention, target:UnitInfo) {
    if (target.visible) {
      if (intent.unit.target != target || intent.unit.commandFrame < With.frame - 24) {
        intent.unit.base.attack(target.base)
      }
      sleepAttack(intent.unit)
    } else {
      move(intent, target.pixelCenter)
    }
  }
  
  def move(intent:Intention, to:Pixel) {
    
    //Send flying units past their destination to maximize acceleration
    val flyingOvershoot = 144.0
    var destination = to
    if (intent.unit.flying && intent.unit.pixelDistanceSquared(to) < Math.pow(flyingOvershoot, 2)) {
      val overshoot = intent.unit.pixelCenter.project(to, flyingOvershoot)
      if (overshoot.valid) destination = overshoot
    }
    
    // Mineral walk!
    if (intent.unit.unitClass.isWorker) {
      val from = intent.unit.pixelCenter
      val fromZone = from.zone
      val toZone = to.zone
      val walkableMineral = toZone.bases
        .flatten(_.minerals)
        .find(mineral =>
          mineral.visible && (
            toZone != fromZone ||
            Math.abs(from.degreesTo(to) - from.degreesTo(mineral.pixelCenter)) < 30))
      if (walkableMineral.isDefined) {
        intent.unit.base.gather(walkableMineral.get.base)
        sleepMove(intent.unit)
        return
      }
    }
    
    // According to https://github.com/tscmoo/tsc-bwai/commit/ceb13344f5994d28d6b601cef126f264ca97426b
    // ordering moves to the exact same destination causes Brood War to not recalculate the path.
    //
    // That means that if the unit got confused while executing the original order, it will remain confused.
    // Issuing an order to a slightly different position can cause Brood War to recalculate the path and un-stick the unit.
    //
    // However, this recalculation can itslef sometimes cause units to get stuck on obstacles.
    // Specifically, units tend to get stuck on buildings this way.
    // The neutral buildings on Roadrunner frequently cause this.
    //
    // So we'll try to get the best of both worlds, and recalculate paths *occasionally*
    if (With.configuration.enablePathRecalculation) {
      destination = destination.add((With.frame / With.configuration.pathRecalculationDelayFrames) % 3 - 1, 0)
    }
    
    if (intent.unit.pixelDistanceFast(destination) > 7) {
      intent.unit.base.move(destination.bwapi)
      sleepMove(intent.unit)
    }
  }
  
  def gather(intent:Intention, resource:UnitInfo) {
    if (intent.unit.carryingMinerals || intent.unit.carryingGas) {
      if ( ! intent.unit.gatheringGas && ! intent.unit.gatheringMinerals) {
        intent.unit.base.returnCargo
        sleepReturnCargo(intent.unit)
      }
    }
    // The logic of "If we're not carrying resources, spam gather until the unit's target is the intended resource"
    // produces mineral locking, in which workers mine more efficiently because exactly 2 miners saturate a mineral patch.
    else if ( ! intent.unit.target.exists(_ == resource)) {
      // TODO: This will fail if we've never seen the resource before, as with some long-distance mining situations.
      // In that case we should order units to move to the destination first.
      if (resource.visible) {
        intent.unit.base.gather(resource.base)
      }
      else {
        move(intent, resource.pixelCenter)
        sleepMove(intent.unit)
      }
    }
  }
  
  def build(intent:Intention, unitClass:UnitClass) {
    intent.unit.base.build(unitClass.baseType)
    sleepBuild(intent.unit)
  }
  
  def build(intent:Intention, unitClass:UnitClass, tile:Tile) {
    if (intent.unit.pixelDistanceSquared(tile.pixelCenter) > Math.pow(32.0 * 5.0, 2)) {
      return move(intent, tile.pixelCenter)
    }
    intent.unit.base.build(unitClass.baseType, tile.bwapi)
    sleepBuild(intent.unit)
  }
  
  def tech(intent:Intention, tech: Tech) {
    intent.unit.base.research(tech.baseType)
  }
  
  def upgrade(intent:Intention, upgrade: Upgrade) {
    intent.unit.base.upgrade(upgrade.baseType)
  }
  
  def buildScarab(intent:Intention) {
    intent.unit.base.build(Protoss.Scarab.baseType)
    sleepMove(intent.unit)
  }
  
  def buildInterceptor(intent:Intention) {
    intent.unit.base.build(Protoss.Interceptor.baseType)
    sleepMove(intent.unit)
  }
  
  private def sleepMove(unit:FriendlyUnitInfo) {
    sleep(unit, 2)
  }
  
  private def sleepAttack(unit:FriendlyUnitInfo) {
    sleep(unit, 2 + unit.unitClass.framesRequiredForAttackToComplete)
  }
  
  private def sleepBuild(unit:FriendlyUnitInfo) {
    //Based on https://github.com/tscmoo/tsc-bwai/blame/master/src/unit_controls.h#L1497
    sleep(unit, 7)
  }
  
  private def sleepReturnCargo(unit:FriendlyUnitInfo) {
    // Based on https://github.com/tscmoo/tsc-bwai/blame/master/src/unit_controls.h#L1442
    sleep(unit, 8)
  }
  
  private def sleep(unit:FriendlyUnitInfo, requiredDelay:Int) {
    Math.max(With.latency.turnSize, requiredDelay)
    nextOrderFrame.put(unit, With.frame + requiredDelay)
  }
}
