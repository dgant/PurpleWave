package Micro.Agency

import Debugging.Visualizations.Views.Micro.ShowUnitsFriendly
import Lifecycle.With
import Mathematics.Points.{Pixel, SpecificPoints, Tile}
import Mathematics.PurpleMath
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.Upgrades.Upgrade
import Utilities.CountMap
import bwapi.UnitCommandType

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
    unit.baseUnit.stop()
    sleep(unit)
  }
  
  def hold(unit: FriendlyUnitInfo) {
    if (unready(unit)) return
    if (unit.moving && ! unit.holdingPosition) {
      unit.baseUnit.holdPosition()
    }
    if (unit.matchups.targetsInRange.nonEmpty) {
      sleepAttack(unit)
    }
    else {
      sleep(unit)
    }
  }
  
  def attack(unit: FriendlyUnitInfo, target: UnitInfo) {
    if (unready(unit)) return
    if ( ! unit.readyForAttackOrder) { // Necessary or redundant?
      sleep(unit)
      return
    }
  
    // TODO: Fix attack cancelling for Photon Cannons
    if (unit.is(Protoss.PhotonCannon)) return
    
    if (target.visible) {
      lazy val moving           = unit.moving
      lazy val alreadyInRange   = unit.inRangeToAttack(target)
      lazy val overdueToAttack  = unit.cooldownLeft == 0 && With.framesSince(unit.lastFrameStartingAttack) > 2.0 * unit.cooldownMaxAirGround
      lazy val thisIsANewTarget = ! unit.orderTarget.contains(target)
      
      val shouldOrder = (
        thisIsANewTarget
        || (overdueToAttack && (moving || alreadyInRange))
        || (target.isFriendly && unit.is(Protoss.Carrier))) // Carrier warmup
      
      if (shouldOrder) {
        unit.baseUnit.attack(target.baseUnit)
        if (ShowUnitsFriendly.inUse && With.visualization.map) {
          ShowUnitsFriendly.drawAttackCommand(unit, target)
        }
      }
      sleepAttack(unit)
    } else {
      move(unit, target.pixelCenter)
    }
  }
  
  private def limit(unit: FriendlyUnitInfo, destination: Pixel): Pixel = {
    Pixel(
      PurpleMath.clamp(destination.x, unit.unitClass.width  / 2,  With.mapPixelWidth  - unit.unitClass.width  / 2),
      PurpleMath.clamp(destination.y, unit.unitClass.height / 2,  With.mapPixelHeight - unit.unitClass.height / 2))
  }

  def attackMove(unit: FriendlyUnitInfo, destination: Pixel) {
    if (unready(unit)) return
    
    val alreadyAttackMovingThere = unit.command.exists(c =>
      c.getUnitCommandType.toString == UnitCommandType.Attack_Move.toString &&
      new Pixel(c.getTargetPosition).pixelDistance(destination) < 128)
    
    if ( ! alreadyAttackMovingThere || unit.seeminglyStuck) {
      unit.baseUnit.attack(destination.bwapi)
    }
    
    sleepAttack(unit)
  }
  
  def patrol(unit: FriendlyUnitInfo, destination: Pixel) {
    if (unready(unit)) return
    
    unit.baseUnit.patrol(destination.bwapi)
    
    sleepAttack(unit)
  }
  
  val flyingOvershoot = 288.0
  def move(unit: FriendlyUnitInfo, to: Pixel) {
    if (unready(unit)) return
    
    // Send flying units past their destination to maximize acceleration
    var destination = to
    if (unit.flying && unit.pixelDistanceSquared(to) < flyingOvershoot * flyingOvershoot) {
      destination = unit.pixelCenter.project(to, flyingOvershoot)
      if (destination == unit.pixelCenter) {
        val signX = PurpleMath.forcedSignum(SpecificPoints.middle.x - destination.x)
        val signY = PurpleMath.forcedSignum(SpecificPoints.middle.y - destination.y)
        destination = destination.add((signX * flyingOvershoot).toInt, (signY * flyingOvershoot).toInt)
      }
    }
    
    // Limit moves to map edge
   destination = limit(unit, destination)
    
    // Record the destination. This is mostly for diagnostic purposes (and identifying stuck units) so if the exact value changes later that's okay
    unit.agent.movingTo = Some(destination)
    
    // Mineral walk!
    if (unit.unitClass.isWorker
      && With.strategy.map.forall(_.mineralWalkingOkay)
      && unit.agent.toBuild.isEmpty
      && ! unit.carryingMinerals
    ) {
      val from      = unit.pixelCenter
      val fromZone  = from.zone
      val toZone    = to.zone
      if (fromZone != toZone || unit.agent.toBuild.isEmpty) {
        val walkableMineral = toZone.bases
          .flatMap(_.minerals)
          .find(mineral =>
            mineral.visible && //Can't mineral walk to an invisible mineral
            mineral.pixelDistanceEdge(unit) > 60.0 &&
            (
              //Don't get stuck by trying to mineral walk through a mineral
              toZone != fromZone ||
              Math.abs(from.degreesTo(to) - from.degreesTo(mineral.pixelCenter)) < 30
            ))
        if (walkableMineral.isDefined) {
          gather(unit, walkableMineral.get, allowReturningCargo = false)
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
    // However, this recalculation can itself sometimes cause units to get stuck on obstacles.
    // Specifically, units tend to get stuck on buildings this way.
    // The neutral buildings on Roadrunner frequently cause this.
    //
    // So we'll try to get the best of both worlds, and recalculate paths *occasionally*
    //
    // Also, give different units different paths to avoid "conga line" behavior
    //
    destination = destination.add((unit.id + With.frame / 36) % 5 - 2, 0)
    
    if (unit.pixelDistanceCenter(destination) > 3) {
      if (unit.is(Terran.Medic)) {
        unit.baseUnit.attack(destination.bwapi)
      }
      else {
        unit.baseUnit.move(destination.bwapi)
      }
    }
    sleep(unit)
  }
  
  def rightClick(unit: FriendlyUnitInfo, target: UnitInfo) {
    if (unready(unit)) return
    unit.baseUnit.rightClick(target.baseUnit)
    sleepAttack(unit)
  }
  
  def useTech(unit: FriendlyUnitInfo, tech: Tech) {
    if (unready(unit)) return
    if (tech == Terran.Stim) {
      if (With.framesSince(unit.agent.lastStim) < 24) return
      unit.agent.lastStim = With.frame
    }
    unit.baseUnit.useTech(tech.baseType)
    sleep(unit)
  }
  
  def useTechOnUnit(unit: FriendlyUnitInfo, tech: Tech, target: UnitInfo) {
    if (unready(unit)) return
    unit.baseUnit.useTech(tech.baseType, target.baseUnit)
    if (tech == Protoss.ArchonMeld || tech == Protoss.DarkArchonMeld) {
      sleep(unit, 48)
    }
    else {
      sleep(unit)
    }
  }
  
  def useTechOnPixel(unit: FriendlyUnitInfo, tech: Tech, target: Pixel) {
    if (unready(unit)) return
    unit.agent.movingTo = Some(target)
    unit.baseUnit.useTech(tech.baseType, target.bwapi)
    if (tech == Terran.SpiderMinePlant) {
      sleep(unit, 12)
    } else {
      sleepAttack(unit)
    }
  }
  
  def repair(unit: FriendlyUnitInfo, target: UnitInfo) {
    if (unready(unit)) return
    unit.baseUnit.repair(target.baseUnit)
    sleep(unit, 24)
  }

  def returnCargo(unit: FriendlyUnitInfo): Unit = {
    if (unit.carryingResources) {
      unit.baseUnit.returnCargo
      sleepReturnCargo(unit)
    }
  }
  
  def gather(unit: FriendlyUnitInfo, resource: UnitInfo, allowReturningCargo: Boolean = true) {
    if (unready(unit)) return
    
    if (allowReturningCargo && (unit.carryingMinerals || unit.carryingGas)) {
      if ( ! unit.gatheringGas && ! unit.gatheringMinerals) {
        returnCargo(unit)
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
        unit.baseUnit.gather(resource.baseUnit)
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
    unit.baseUnit.build(unitClass.baseType)
    sleepBuild(unit)
  }
  
  def build(unit: FriendlyUnitInfo, unitClass: UnitClass, tile: Tile) {
    if (unready(unit)) return
    if (unit.pixelDistanceSquared(tile.pixelCenter) > Math.pow(32.0 * 5.0, 2)) {
      move(unit, tile.pixelCenter)
      return
    }
    unit.baseUnit.build(unitClass.baseType, tile.bwapi)
    sleepBuild(unit)
  }
  
  def tech(unit: FriendlyUnitInfo, tech: Tech) {
    if (unready(unit)) return
    unit.baseUnit.research(tech.baseType)
    sleep(unit)
  }
  
  def upgrade(unit: FriendlyUnitInfo, upgrade: Upgrade) {
    if (unready(unit)) return
    unit.baseUnit.upgrade(upgrade.baseType)
    sleep(unit)
  }
  
  def cancel(unit: FriendlyUnitInfo) {
    if (unready(unit)) return
    unit.baseUnit.cancelConstruction()
    sleep(unit)
  }
  
  def rally(unit: FriendlyUnitInfo, pixel: Pixel) {
    if (unready(unit)) return
    unit.baseUnit.setRallyPoint(pixel.bwapi)
    unit.lastSetRally = With.frame
    sleep(unit)
  }
  
  def rally(unit: FriendlyUnitInfo, targetUnit: UnitInfo) {
    if (unready(unit)) return
    unit.baseUnit.setRallyPoint(unit.baseUnit)
    unit.lastSetRally = With.frame
    sleep(unit)
  }
  
  
  def unload(unit: FriendlyUnitInfo, passenger: UnitInfo) {
    //No sleeping required
    unit.baseUnit.unload(passenger.baseUnit)
  }
  
  def addon(unit: FriendlyUnitInfo, unitClass: UnitClass) {
    if (unready(unit)) return
    unit.baseUnit.buildAddon(unitClass.baseType)
    sleep(unit)
  }
  
  def buildScarab(unit: FriendlyUnitInfo) {
    if (unready(unit)) return
    unit.baseUnit.build(Protoss.Scarab.baseType)
    sleep(unit)
  }
  
  def buildInterceptor(unit: FriendlyUnitInfo) {
    if (unready(unit)) return
    unit.baseUnit.build(Protoss.Interceptor.baseType)
    sleep(unit)
  }
  
  def cloak(unit: FriendlyUnitInfo, tech: Tech) {
    if (unready(unit)) return
    unit.agent.lastCloak = With.frame
    unit.baseUnit.cloak()
    sleep(unit)
  }
  
  def decloak(unit: FriendlyUnitInfo, tech: Tech) {
    if (unready(unit)) return
    unit.baseUnit.decloak()
    sleep(unit)
  }
  
  def burrow(unit: FriendlyUnitInfo) {
    if (unready(unit)) return
    unit.baseUnit.burrow()
    sleep(unit)
  }
  
  def unburrow(unit: FriendlyUnitInfo) {
    if (unready(unit)) return
    unit.baseUnit.unburrow()
    sleep(unit)
  }
  
  def lift(unit: FriendlyUnitInfo) {
    if (unready(unit)) return
    unit.baseUnit.lift()
    sleep(unit)
  }
  
  def pass(unit: FriendlyUnitInfo) {
    if (unready(unit)) return
    sleep(unit)
  }
  
  private def sleepAttack(unit: FriendlyUnitInfo) {
    sleep(unit, AttackDelay.framesToWaitAfterIssuingAttackOrder(unit))
  }
  
  private def sleepBuild(unit: FriendlyUnitInfo) {
    //Based on https://github.com/tscmoo/tsc-bwai/blame/master/src/unit_controls.h#L1497
    sleep(unit, 1 + 2 * With.latency.latencyFrames)
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
