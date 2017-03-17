package Micro

import Micro.Behaviors.Behavior
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Startup.With
import Utilities.CountMap
import bwapi.Position

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
  
  def attack(unit:FriendlyUnitInfo, target:UnitInfo) {
    unit.baseUnit.attack(target.baseUnit)
    sleep(unit, true)
  }
  
  def move(unit:FriendlyUnitInfo, position:Position) {
    unit.baseUnit.move(position)
    sleep(unit, false)
  }
  
  def gather(unit:FriendlyUnitInfo, resource:UnitInfo) {
    unit.baseUnit.gather(resource.baseUnit)
    sleep(unit, false)
  }
  
  def buildScarab(unit:FriendlyUnitInfo) {
    unit.baseUnit.build(Protoss.Scarab.baseType)
    sleep(unit, false)
  }
  
  def buildInterceptor(unit:FriendlyUnitInfo) {
    unit.baseUnit.build(Protoss.Interceptor.baseType)
    sleep(unit, false)
  }
  
  private def sleep(unit:FriendlyUnitInfo, startedAttacking:Boolean = false) {
    val baseDelay = With.game.getRemainingLatencyFrames
    val attackDelay = if (startedAttacking) unit.attackFrames else 0
    nextOrderFrame.put(unit, baseDelay + attackDelay + With.frame)
  }
  
  private def recordCommand(unit:FriendlyUnitInfo, command:Behavior) {
    if (With.configuration.enableVisualizationUnitsOurs) {
      lastCommands.put(unit, command.getClass.getSimpleName.replace("$", ""))
    }
  }
}
