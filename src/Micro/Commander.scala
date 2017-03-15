package Micro

import BWMirrorProxy.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Micro.Behaviors.Behavior
import Micro.Intentions.Intention
import Startup.With
import Utilities.CountMap
import bwapi.{Position, UnitType}

import scala.collection.mutable

class Commander {
  
  private var intentions = new mutable.HashMap[FriendlyUnitInfo, Intention]
  private val nextOrderFrame = new CountMap[FriendlyUnitInfo]
  
  val lastCommands = new mutable.HashMap[FriendlyUnitInfo, String]
  var lastIntentions = new mutable.HashMap[FriendlyUnitInfo, Intention]
  
  def intend(intention:Intention) = intentions.put(intention.unit, intention)
  
  def onFrame() {
    val awakeIntentions = intentions.filter(pair => isAwake(pair._1))
    awakeIntentions.foreach(pair => pair._2.command.execute(pair._2))
    nextOrderFrame.keySet.filterNot(_.alive).foreach(nextOrderFrame.remove)
    lastIntentions --= lastIntentions.keys.filterNot(_.alive)
    awakeIntentions.foreach(pair => lastIntentions.put(pair._1, pair._2))
    intentions = new mutable.HashMap[FriendlyUnitInfo, Intention]
  }
  
  def attack(unit:FriendlyUnitInfo, target:UnitInfo) {
    //_recordCommand(unit, command)
    unit.baseUnit.attack(target.baseUnit)
    sleep(unit, true)
  }
  
  def move(unit:FriendlyUnitInfo, position:Position) {
    //_recordCommand(unit, command)
    unit.baseUnit.move(position)
    sleep(unit, false)
  }
  
  def gather(unit:FriendlyUnitInfo, resource:UnitInfo) {
    //_recordCommand(unit, command)
    unit.baseUnit.gather(resource.baseUnit)
    sleep(unit, false)
  }
  
  def buildScarab(unit:FriendlyUnitInfo) {
    //_recordCommand(unit, command)
    unit.baseUnit.build(UnitType.Protoss_Scarab)
    sleep(unit, false)
  }
  
  def buildInterceptor(unit:FriendlyUnitInfo) {
    //_recordCommand(unit, command)
    unit.baseUnit.build(UnitType.Protoss_Interceptor)
    sleep(unit, false)
  }
  
  private def sleep(unit:FriendlyUnitInfo, startedAttacking:Boolean = false) {
    val baseDelay = With.game.getRemainingLatencyFrames
    val attackDelay = if (startedAttacking) unit.attackFrames else 0
    nextOrderFrame.put(unit, baseDelay + attackDelay + With.frame)
  }
  
  private def isAwake(unit:FriendlyUnitInfo):Boolean = {
    nextOrderFrame(unit) < With.frame
  }
  
  private def isAwake(intent:Intention):Boolean = {
    isAwake(intent.unit)
  }
  
  private def recordCommand(unit:FriendlyUnitInfo, command:Behavior) {
    if (With.configuration.enableVisualizationUnitsOurs) {
      lastCommands.put(unit, command.getClass.getSimpleName.replace("$", ""))
    }
  }
}
