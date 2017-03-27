package Micro

import Micro.Behaviors.Behavior
import Micro.Intentions.Intention
import ProxyBwapi.Races.Protoss
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.Upgrades.Upgrade
import Startup.With
import Utilities.{CountMap, RandomState}
import Utilities.EnrichPosition._
import bwapi.{Position, TilePosition, UnitCommandType}

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
     || intent.unit.command.getTarget != target.base) {
      intent.unit.base.attack(target.base)
    }
    sleepAttack(intent.unit)
  }
  
  def move(intent:Intention, position:Position) {
    
    //Make melee units sticky
    val stickyMeleeTarget = intent.toAttack
      .map(_.pixelCenter)
      .filter(targetPosition =>
        targetPosition.pixelDistance(position) < With.configuration.combatStickinessLeash
        && intent.unit.unitClass.maxAirGroundRange <= With.configuration.combatStickinessLeash)
    
    if (stickyMeleeTarget.isDefined) return attack(intent, intent.toAttack.get)
    
    val flyingOvershoot = 128.0
    var destination = position
    if (intent.unit.flying && intent.unit.pixelDistance(position) < flyingOvershoot) {
      //Send flying units further to maximize acceleration
      destination = intent.unit.pixelCenter.project(position, flyingOvershoot)
    }
    
    //According to https://github.com/tscmoo/tsc-bwai/commit/ceb13344f5994d28d6b601cef126f264ca97426b
    //ordering moves to the exact same destination causes Brood War to not recalculate the path.
    //Better to recalculate the path a few times to prevent units getting stuck
    if (With.configuration.enablePathRecalculation) {
      intent.unit.base.move(destination.add(
        RandomState.random.nextInt(5) - 2,
        RandomState.random.nextInt(5) - 2))
    }
    else {
      intent.unit.base.move(destination)
    }
    sleepMove(intent.unit)
  }
  
  def gather(intent:Intention, resource:UnitInfo) {
    val command = intent.unit.command
    if (command.getUnitCommandType != UnitCommandType.Right_Click_Unit || command.getTarget == null || command.getTarget.getID != resource.id) {
      //TMP: Disabled
      if (false) {
        if (intent.unit.carryingGas || intent.unit.carryingMinerals) {
          val townHalls = With.units.ours.filter(_.unitClass.isTownHall).filter(_.complete)
          if (townHalls.nonEmpty) {
            val nearestTownHall = townHalls.minBy(townHall => With.paths.groundPixels(resource.tileCenter, townHall.tileCenter))
            intent.unit.base.rightClick(nearestTownHall.base)
            sleepReturnCargo(intent.unit)
            return
          }
        }
      }
      
      //TODO: This will fail if we've never seen the resource before
      intent.unit.base.rightClick(resource.base)
    }
  }
  
  def build(intent:Intention, unitClass:UnitClass) {
    intent.unit.base.build(unitClass.baseType)
    sleepBuild(intent.unit)
  }
  
  def build(intent:Intention, unitClass:UnitClass, tile:TilePosition) {
    if (intent.unit.tileDistance(tile) > 32 * 5) {
      return move(intent, tile.pixelCenter)
    }
    intent.unit.base.build(unitClass.baseType, tile)
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
    sleep(unit, 4)
  }
  
  private def sleepAttack(unit:FriendlyUnitInfo) {
    sleep(unit, unit.requiredAttackDelay)
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
  
  private def recordCommand(unit:FriendlyUnitInfo, command:Behavior) {
    if (With.configuration.enableVisualizationUnitsOurs) {
      lastCommands.put(unit, command.getClass.getSimpleName.replace("$", ""))
    }
  }
}
