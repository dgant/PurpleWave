package Micro

import Micro.Behaviors.Behavior
import Micro.Intentions.Intention
import Micro.Movement.MovementRandom
import ProxyBwapi.Races.Protoss
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.Upgrades.Upgrade
import Startup.With
import Utilities.CountMap
import bwapi.{Position, TilePosition, UnitCommandType}
import Utilities.TypeEnrichment.EnrichPosition._

import scala.collection.mutable

// Commander is responsible for issuing unit commands
// in a way that Brood War handles gracefully.
//
// The goal is for the rest of the code base to be blissfully unaware
// of Brood War's glitchy unit behavior.
//
class Commander {
  
  private val nextOrderFrame = new CountMap[FriendlyUnitInfo]
  val lastCommands = new mutable.HashMap[FriendlyUnitInfo, String]
  
  def onFrame() {
    nextOrderFrame.keySet.filterNot(_.alive).foreach(nextOrderFrame.remove)
  }
  
  def readyForCommand(unit:FriendlyUnitInfo):Boolean = {
    nextOrderFrame(unit) < With.frame
  }
  
  def attack(intent:Intention, target:UnitInfo) {
    if (intent.unit.command.getUnitCommandType != UnitCommandType.Attack_Unit
     || intent.unit.command.getTarget != target.baseUnit) {
      intent.unit.baseUnit.attack(target.baseUnit)
    }
    sleepAttack(intent.unit)
  }
  
  def move(intent:Intention, position:Position) {
    //According to https://github.com/tscmoo/tsc-bwai/commit/ceb13344f5994d28d6b601cef126f264ca97426b
    //ordering moves to the exact same destination causes Brood War to not recalculate the path.
    //Better to recalculate the path a few times to prevent units getting stuck
    intent.unit.baseUnit.move(position.add(
      MovementRandom.random.nextInt(5) - 2,
      MovementRandom.random.nextInt(5) - 2))
    sleepMove(intent.unit)
  }
  
  def gather(intent:Intention, resource:UnitInfo) {
    if (intent.unit.command.getTarget != resource.baseUnit) {
      if (intent.unit.isCarryingGas || intent.unit.isCarryingMinerals) {
        val townHalls = With.units.ours.filter(_.unitClass.isTownHall).filter(_.complete)
        if (townHalls.nonEmpty) {
          val nearestTownHall = townHalls.minBy(townHall => With.paths.groundDistance(resource.tileCenter, townHall.tileCenter))
          intent.unit.baseUnit.rightClick(nearestTownHall.baseUnit)
          sleepReturnCargo(intent.unit)
          return
        }
      }
      
      intent.unit.baseUnit.rightClick(resource.baseUnit)
      sleepMove(intent.unit)
    }
  }
  
  def build(intent:Intention, unitClass:UnitClass) {
    intent.unit.baseUnit.build(unitClass.baseType)
    sleepBuild(intent.unit)
  }
  
  def build(intent:Intention, unitClass:UnitClass, tile:TilePosition) {
    if (intent.unit.distance(tile) > 32 * 5) {
      return move(intent, tile.pixelCenter)
    }
    intent.unit.baseUnit.build(unitClass.baseType, tile)
    sleepBuild(intent.unit)
  }
  
  def tech(intent:Intention, tech: Tech) {
    intent.unit.baseUnit.research(tech.baseType)
  }
  
  def upgrade(intent:Intention, upgrade: Upgrade) {
    intent.unit.baseUnit.upgrade(upgrade.baseType)
  }
  
  def buildScarab(intent:Intention) {
    intent.unit.baseUnit.build(Protoss.Scarab.baseType)
    sleepMove(intent.unit)
  }
  
  def buildInterceptor(intent:Intention) {
    intent.unit.baseUnit.build(Protoss.Interceptor.baseType)
    sleepMove(intent.unit)
  }
  
  private def sleepMove(unit:FriendlyUnitInfo) {
    sleep(unit, 0)
  }
  
  private def sleepAttack(unit:FriendlyUnitInfo) {
    sleep(unit, unit.attackFrames)
  }
  
  private def sleepBuild(unit:FriendlyUnitInfo) {
    //Based on https://github.com/tscmoo/tsc-bwai/blame/master/src/unit_controls.h#L1497
    sleep(unit, 7)
  }
  
  private def sleepReturnCargo(unit:FriendlyUnitInfo) {
    // Based on https://github.com/tscmoo/tsc-bwai/blame/master/src/unit_controls.h#L1442
    sleep(unit, 8)
  }
  
  private def sleep(unit:FriendlyUnitInfo, extraDelay:Int) {
    val baseDelay = With.game.getRemainingLatencyFrames
    nextOrderFrame.put(unit, With.frame + baseDelay + extraDelay)
  }
  
  private def recordCommand(unit:FriendlyUnitInfo, command:Behavior) {
    if (With.configuration.enableVisualizationUnitsOurs) {
      lastCommands.put(unit, command.getClass.getSimpleName.replace("$", ""))
    }
  }
}
