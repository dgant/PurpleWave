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
  
    // https://docs.google.com/spreadsheets/d/1bsvPvFil-kpvEUfSG74U3E5PLSTC02JxSkiR8QdLMuw/edit#gid=0
    //
    // "Dragoon, Devourer only units that can have damage prevented by stop() too early"
    //
    // According to JohnJ: "After the frame where isStartingAttack is true, it should be left alone for the next 8 frames"
    // "I assume that frame count is in ignorance of latency, so if i issue an order before the 8 frames are up that would be executed on the first frame thereafter, i'm in the clear?"
    // JohnJ: yeah in theory. actually in practice. I did test that.
    //
    // JohnJ's research says (now + 9 - frames before execution) is the exact timing
    // That formula causes Dragoons to miss some shots, especially on consecutive attacks without moving.
    // So I've added one more frame of delay. Consecutive attacks are still sometimes missing.
    //
    // WARNING: This requires micro to run on *every frame*!
    //
    nextOrderFrame.keys
      .filter(unit => unit.is(Protoss.Dragoon))
      .filter(unit => unit.is(Protoss.Dragoon))
      .foreach(dragoon => nextOrderFrame(dragoon) = Math.max(nextOrderFrame(dragoon), dragoon.lastAttackFrame + 10 - With.latency.latencyFrames))
  }
  
  def ready(unit: FriendlyUnitInfo): Boolean = {
    nextOrderFrame(unit) < With.frame
  }
  
  private def unready(unit: FriendlyUnitInfo): Boolean = {
    ! ready(unit)
  }
  
  def cancel(unit: FriendlyUnitInfo) {
    unit.base.cancelConstruction()
    sleep(unit)
  }
  
  def stop(unit: FriendlyUnitInfo) {
    unit.base.stop()
    sleep(unit)
  }
  
  def attack(unit: FriendlyUnitInfo, target: UnitInfo) {
    if (unready(unit)) return
    
    if (unit.is(Protoss.Carrier)) {
      //tryCarrierAttack(unit, target)
      //if (unready(unit)) return
    }
    
    if (target.visible) {
      if (unit.canAttackThisFrame || ! unit.target.contains(target)) {
        unit.base.attack(target.base)
      }
      sleepAttack(unit)
    } else {
      move(unit, target.pixelCenter)
    }
  }
  
  def tryCarrierAttack(unit: FriendlyUnitInfo, target: UnitInfo) {
    if (unready(unit)) return
  
    // Carriers are finicky. Manage them appropriately.
    // * Don't attack units that aren't huge. Just attack-move to keep the interceptors going
    // * Don't disturb interceptors that are already moving
    //
    // According to tscmoo, Carrier leash range is 10 (not the 12 on Liquipedia)
    if (target.unitClass.maxTotalHealth >= 400) {
      unit.base.attack(target.base)
      sleepAttack(unit)
      return
    }
    val destination = target.pixelCenter
    if (target.totalHealth < 400) {
      if ( ! unit.interceptors.exists(_.attacking)) {
        // TODO: This is probably incorrect
        attackMove(unit, target.pixelCenter)
        sleepAttack(unit)
      }
    }
  }
  
  def attackMove(unit: FriendlyUnitInfo, destination: Pixel) {
    if (unready(unit)) return
    
    unit.base.attack(destination.bwapi)
    sleepAttack(unit)
  }
  
  def move(unit: FriendlyUnitInfo, to: Pixel) {
    if (unready(unit)) return
    
    //Send flying units past their destination to maximize acceleration
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
    sleep(unit, unit.unitClass.framesRequiredForAttackToComplete + With.latency.turnSize - 1) //After attack completes, ready on first frame of following turn
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
