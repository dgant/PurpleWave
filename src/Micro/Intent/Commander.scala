package Micro.Intent

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.Races.Protoss
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.Upgrades.Upgrade
import Utilities.CountMap

// Commander is responsible for issuing unit commands
// in a way that Brood War handles gracefully,
// and to take advantage of Brood War mechanics to optimize commands.
//
// The goal is for the rest of the code base to be blissfully unaware
// of Brood War's glitchy unit behavior.
//
class Commander {
  
  private val nextOrderFrame = new CountMap[FriendlyUnitInfo]
  
  def run() {
    nextOrderFrame.keys.filterNot(_.alive).foreach(nextOrderFrame.remove)
    nextOrderFrame.keys
      .filter(unit => unit.is(Protoss.Dragoon))
      .foreach(dragoon => nextOrderFrame(dragoon) = Math.max(nextOrderFrame(dragoon), DragoonDelay.nextSafeFrameToOrder(dragoon)))
  }
  
  def ready(unit: FriendlyUnitInfo): Boolean = {
    nextOrderFrame(unit) <= With.frame
  }
  
  private def unready(unit: FriendlyUnitInfo): Boolean = {
    ! ready(unit)
  }
  
  def doNothing(unit: FriendlyUnitInfo) {
    if (unready(unit)) return
    sleep(unit)
  }
  
  def stop(unit: FriendlyUnitInfo) {
    if (unready(unit)) return
    unit.base.stop()
    sleep(unit)
  }
  
  def hold(unit: FriendlyUnitInfo) {
    if (unready(unit)) return
    if ( ! unit.holdingPosition) {
      unit.base.holdPosition()
    }
    sleep(unit)
  }
  
  def attack(unit: FriendlyUnitInfo, target: UnitInfo) {
    if (unready(unit)) return
    
    if (unit.interceptors > 0) {
      unit.base.attack(target.pixelCenter.bwapi)
    }
    else if (target.visible) {
      if (unit.readyForAttackOrder || ! unit.target.contains(target) || With.framesSince(unit.lastAttackStartFrame) > unit.cooldownMaxAirGround * 1.5) {
        unit.base.attack(target.base)
      }
      sleepAttack(unit)
    } else {
      move(unit, target.pixelCenter)
    }
  }
  
  def attackMove(unit: FriendlyUnitInfo, destination: Pixel) {
    if (unready(unit)) return
    
    unit.base.attack(destination.bwapi)
    sleepAttack(unit)
  }
  
  def move(unit: FriendlyUnitInfo, to: Pixel) {
    if (unready(unit)) return
    
    // Send flying units past their destination to maximize acceleration
    val flyingOvershoot = 144.0
    var destination = to
    if (unit.flying && unit.pixelDistanceSquared(to) < Math.pow(flyingOvershoot, 2)) {
      val overshoot = unit.pixelCenter.project(to, flyingOvershoot)
      if (overshoot.valid) destination = overshoot
    }
    
    // Mineral walk!
    if (unit.unitClass.isWorker && ! unit.carryingResources
      && ! With.mapFileName.toLowerCase.contains("barrier") //Hack -- Great Barrier Reef is ruinous for automatic mineral walking
    ) {
      val from      = unit.pixelCenter
      val fromZone  = from.zone
      val toZone    = to.zone
      if (fromZone != toZone || With.configuration.enableMineralWalkInSameZone) {
        val walkableMineral = toZone.bases
          .flatMap(_.minerals)
          .find(mineral =>
            mineral.visible && //Can't mineral walk to an invisible mineral
            mineral.pixelsFromEdgeFast(unit) > 60.0 &&
            (
              //Don't get stuck by trying to mineral walk through a mineral
              toZone != fromZone ||
              Math.abs(from.degreesTo(to) - from.degreesTo(mineral.pixelCenter)) < 30
            ))
        if (walkableMineral.isDefined) {
          gather(unit, walkableMineral.get)
          return
        }
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
    
    if (unit.pixelDistanceFast(destination) > 7) {
      unit.base.move(destination.bwapi)
      sleep(unit)
    }
  }
  
  def useTechOnUnit(unit: FriendlyUnitInfo, tech: Tech, target: UnitInfo) {
    if (unready(unit)) return
    unit.base.useTech(tech.baseType, target.base)
    sleep(unit)
  }
  
  def useTechOnPixel(unit: FriendlyUnitInfo, tech: Tech, target: Pixel) {
    if (unready(unit)) return
    unit.base.useTech(tech.baseType, target.bwapi)
    sleepAttack(unit)
  }
  
  def gather(unit: FriendlyUnitInfo, resource: UnitInfo) {
    if (unready(unit)) return
    
    if (unit.carryingMinerals || unit.carryingGas) {
      if ( ! unit.gatheringGas && ! unit.gatheringMinerals) {
        unit.base.returnCargo
        sleepReturnCargo(unit)
      }
      else {
        sleep(unit)
      }
    }
      
    // The logic of "If we're not carrying resources, spam gather until the unit's target is the intended resource"
    // produces mineral locking, in which workers mine more efficiently because exactly 2 miners saturate a mineral patch.
    //
    else if ( ! unit.target.contains(resource)) {
      if (resource.visible) {
        unit.base.gather(resource.base)
        sleep(unit)
      }
      else {
        move(unit, resource.pixelCenter)
      }
    }
    else {
      sleep(unit)
    }
  }
  
  def build(unit: FriendlyUnitInfo, unitClass: UnitClass) {
    if (unready(unit)) return
    unit.base.build(unitClass.baseType)
    sleepBuild(unit)
  }
  
  def build(unit: FriendlyUnitInfo, unitClass: UnitClass, tile: Tile) {
    if (unready(unit)) return
    if (unit.pixelDistanceSquared(tile.pixelCenter) > Math.pow(32.0 * 5.0, 2)) {
      move(unit, tile.pixelCenter)
      return
    }
    unit.base.build(unitClass.baseType, tile.bwapi)
    sleepBuild(unit)
  }
  
  def tech(unit: FriendlyUnitInfo, tech: Tech) {
    if (unready(unit)) return
    unit.base.research(tech.baseType)
    sleep(unit)
  }
  
  def upgrade(unit: FriendlyUnitInfo, upgrade: Upgrade) {
    if (unready(unit)) return
    unit.base.upgrade(upgrade.baseType)
    sleep(unit)
  }
  
  
  def cancel(unit: FriendlyUnitInfo) {
    if (unready(unit)) return
    unit.base.cancelConstruction()
    sleep(unit)
  }
  
  def rally(unit: FriendlyUnitInfo, pixel: Pixel) {
    if (unready(unit)) return
    unit.base.setRallyPoint(pixel.bwapi)
    unit.hasSetRallyPoint = true
    sleep(unit)
  }
  
  def rally(unit: FriendlyUnitInfo, targetUnit: UnitInfo) {
    if (unready(unit)) return
    unit.base.setRallyPoint(unit.base)
    unit.hasSetRallyPoint = true
    sleep(unit)
  }
  
  def buildScarab(unit: FriendlyUnitInfo) {
    if (unready(unit)) return
    unit.base.build(Protoss.Scarab.baseType)
    sleep(unit)
  }
  
  def buildInterceptor(unit: FriendlyUnitInfo) {
    if (unready(unit)) return
    unit.base.build(Protoss.Interceptor.baseType)
    sleep(unit)
  }
  
  private def sleepAttack(unit: FriendlyUnitInfo) {
    sleep(unit, With.latency.latencyFrames + 1) // Make sure we don't cancel an attack before the order has executed
  }
  
  private def sleepBuild(unit: FriendlyUnitInfo) {
    //Based on https://github.com/tscmoo/tsc-bwai/blame/master/src/unit_controls.h#L1497
    sleep(unit, 7)
  }
  
  private def sleepReturnCargo(unit:FriendlyUnitInfo) {
    // Based on https://github.com/tscmoo/tsc-bwai/blame/master/src/unit_controls.h#L1442
    sleep(unit, 8)
  }
  
  private def sleep(unit: FriendlyUnitInfo, requiredDelay: Int = 2) {
    val sleepUntil = Array(
      With.frame + With.configuration.performanceMinimumUnitSleep,
      With.frame + requiredDelay,
      With.frame + With.latency.turnSize,
      nextOrderFrame(unit)).max
    nextOrderFrame.put(unit, sleepUntil)
  }
}
