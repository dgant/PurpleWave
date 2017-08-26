package Micro.Agency

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, Orders, UnitInfo}
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
    nextOrderFrame.keys.foreach(unit => nextOrderFrame(unit) = Math.max(nextOrderFrame(unit), AttackDelay.nextSafeOrderFrame(unit)))
  }
  
  def ready(unit: FriendlyUnitInfo): Boolean = {
    nextOrderFrame(unit) <= With.frame
  }
  
  private def unready(unit: FriendlyUnitInfo): Boolean = {
    ! ready(unit)
  }
  
  def hijack(unit: FriendlyUnitInfo) {
    nextOrderFrame.remove(unit)
  }
  
  def doNothing(unit: FriendlyUnitInfo) {
    if (unready(unit)) return
    sleep(unit)
  }
  
  def stop(unit: FriendlyUnitInfo) {
    if (unready(unit)) return
    unit.bwapi.stop()
    sleep(unit)
  }
  
  def hold(unit: FriendlyUnitInfo) {
    if (unready(unit)) return
    if ( ! unit.holdingPosition) {
      unit.bwapi.holdPosition()
    }
    sleep(unit)
  }
  
  def attack(unit: FriendlyUnitInfo, target: UnitInfo) {
    if (unready(unit)) return
  
    // TODO: Fix attack cancelling for Photon Cannons
    if (unit.is(Protoss.PhotonCannon)) return
    
    if (unit.interceptors.exists(_.order == Orders.InterceptorAttack)) {
      // Carriers lose most of their DPS with direct attack orders
      unit.bwapi.attack(target.pixelCenter.bwapi)
    }
    else if (target.visible) {
      if (unit.readyForAttackOrder || ! unit.target.contains(target) || With.framesSince(unit.lastAttackStartFrame) > unit.cooldownMaxAirGround * 1.5) {
        unit.bwapi.attack(target.bwapi)
      }
      sleepAttack(unit)
    } else {
      move(unit, target.pixelCenter)
    }
  }
  
  def attackMove(unit: FriendlyUnitInfo, destination: Pixel) {
    if (unready(unit)) return
    unit.bwapi.attack(destination.bwapi)
    sleepAttack(unit)
  }
  
  def move(unit: FriendlyUnitInfo, to: Pixel) {
    if (unready(unit)) return
    
    // Send flying units past their destination to maximize acceleration
    val flyingOvershoot = 188.0
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
    //
    // Also, give different units different paths to avoid "conga line" behavior
    if (With.configuration.enablePathRecalculation) {
      destination = destination.add((unit.id + With.frame / With.configuration.pathRecalculationDelayFrames) % 5 - 2, 0)
    }
    
    if (unit.pixelDistanceFast(destination) > 0) {
      if (unit.is(Terran.Medic)) {
        unit.bwapi.attack(destination.bwapi)
      }
      else {
        unit.bwapi.move(destination.bwapi)
      }
    }
    sleep(unit)
  }
  
  def rightClick(unit: FriendlyUnitInfo, target: UnitInfo) {
    if (unready(unit)) return
    unit.bwapi.rightClick(target.bwapi)
    sleepAttack(unit)
  }
  
  def useTech(unit: FriendlyUnitInfo, tech: Tech) {
    if (unready(unit)) return
    if (tech == Terran.Stim) {
      if (With.framesSince(unit.agent.lastStim) < 24) return
      unit.agent.lastStim = With.frame
    }
    unit.bwapi.useTech(tech.baseType)
    sleep(unit)
  }
  
  def useTechOnUnit(unit: FriendlyUnitInfo, tech: Tech, target: UnitInfo) {
    if (unready(unit)) return
    unit.bwapi.useTech(tech.baseType, target.bwapi)
    sleep(unit)
  }
  
  def useTechOnPixel(unit: FriendlyUnitInfo, tech: Tech, target: Pixel) {
    if (unready(unit)) return
    unit.bwapi.useTech(tech.baseType, target.bwapi)
    sleepAttack(unit)
  }
  
  def repair(unit: FriendlyUnitInfo, target: UnitInfo) {
    if (unready(unit)) return
    unit.bwapi.repair(target.bwapi)
    sleep(unit, 24)
  }
  
  def gather(unit: FriendlyUnitInfo, resource: UnitInfo) {
    if (unready(unit)) return
    
    if (unit.carryingMinerals || unit.carryingGas) {
      if ( ! unit.gatheringGas && ! unit.gatheringMinerals) {
        unit.bwapi.returnCargo
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
        unit.bwapi.gather(resource.bwapi)
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
    unit.bwapi.build(unitClass.baseType)
    sleepBuild(unit)
  }
  
  def build(unit: FriendlyUnitInfo, unitClass: UnitClass, tile: Tile) {
    if (unready(unit)) return
    if (unit.pixelDistanceSquared(tile.pixelCenter) > Math.pow(32.0 * 5.0, 2)) {
      move(unit, tile.pixelCenter)
      return
    }
    unit.bwapi.build(unitClass.baseType, tile.bwapi)
    sleepBuild(unit)
  }
  
  def tech(unit: FriendlyUnitInfo, tech: Tech) {
    if (unready(unit)) return
    unit.bwapi.research(tech.baseType)
    sleep(unit)
  }
  
  def upgrade(unit: FriendlyUnitInfo, upgrade: Upgrade) {
    if (unready(unit)) return
    unit.bwapi.upgrade(upgrade.baseType)
    sleep(unit)
  }
  
  def cancel(unit: FriendlyUnitInfo) {
    if (unready(unit)) return
    unit.bwapi.cancelConstruction()
    sleep(unit)
  }
  
  def rally(unit: FriendlyUnitInfo, pixel: Pixel) {
    if (unready(unit)) return
    unit.bwapi.setRallyPoint(pixel.bwapi)
    unit.hasSetRallyPoint = true
    sleep(unit)
  }
  
  def rally(unit: FriendlyUnitInfo, targetUnit: UnitInfo) {
    if (unready(unit)) return
    unit.bwapi.setRallyPoint(unit.bwapi)
    unit.hasSetRallyPoint = true
    sleep(unit)
  }
  
  
  def unload(unit: FriendlyUnitInfo, passenger: UnitInfo) {
    //No sleeping required
    unit.bwapi.unload(passenger.bwapi)
  }
  
  def addon(unit: FriendlyUnitInfo, unitClass: UnitClass) {
    if (unready(unit)) return
    unit.bwapi.buildAddon(unitClass.baseType)
    sleep(unit)
  }
  
  def buildScarab(unit: FriendlyUnitInfo) {
    if (unready(unit)) return
    unit.bwapi.build(Protoss.Scarab.baseType)
    sleep(unit)
  }
  
  def buildInterceptor(unit: FriendlyUnitInfo) {
    if (unready(unit)) return
    unit.bwapi.build(Protoss.Interceptor.baseType)
    sleep(unit)
  }
  
  def cloak(unit: FriendlyUnitInfo, tech: Tech) {
    if (unready(unit)) return
    unit.agent.lastCloak = With.frame
    unit.bwapi.cloak()
    sleep(unit)
  }
  
  def decloak(unit: FriendlyUnitInfo, tech: Tech) {
    if (unready(unit)) return
    unit.bwapi.decloak()
    sleep(unit)
  }
  
  private def sleepAttack(unit: FriendlyUnitInfo) {
    sleep(unit, AttackDelay.framesToWaitAfterIssuingAttackOrder(unit))
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
