package Micro

import Micro.Behaviors.Behavior
import Micro.Intentions.Intention
import ProxyBwapi.Races.Protoss
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.Upgrades.Upgrade
import Startup.With
import Utilities.CountMap
import bwapi.{Position, TilePosition}

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
    intent.unit.baseUnit.attack(target.baseUnit)
    sleepAttack(intent.unit)
  }
  
  def move(intent:Intention, position:Position) {
    intent.unit.baseUnit.move(position)
    sleepMove(intent.unit)
  }
  
  def gather(intent:Intention, resource:UnitInfo) {
    if (intent.unit.command.getTarget != resource.baseUnit) {
      
      if (intent.unit.isCarryingGas || intent.unit.isCarryingMinerals) {
        val townHalls = With.units.ours.filter(_.unitClass.isTownHall).filter(_.complete)
        if (townHalls.nonEmpty) {
          val nearestTownHall = townHalls.minBy(townHall => With.paths.groundDistance(intent.unit.tileCenter, townHall.tileCenter))
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
    //Arbitrary.
    sleep(unit, With.latency.minTurnSize * 2)
  }
  
  private def sleepReturnCargo(unit:FriendlyUnitInfo) {
    //Arbitrary
    sleep(unit, 24)
  }
  
  private def sleep(unit:FriendlyUnitInfo, extraDelay:Int) {
    //TODO: Revisit. Should we use turn size?
    val baseDelay = With.game.getRemainingLatencyFrames
    nextOrderFrame.put(unit, With.frame + baseDelay + extraDelay)
  }
  
  private def recordCommand(unit:FriendlyUnitInfo, command:Behavior) {
    if (With.configuration.enableVisualizationUnitsOurs) {
      lastCommands.put(unit, command.getClass.getSimpleName.replace("$", ""))
    }
  }
}
