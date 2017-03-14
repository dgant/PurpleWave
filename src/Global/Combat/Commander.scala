package Global.Combat

import Global.Combat.Behaviors.Behavior
import Startup.With
import Types.Intents.Intention
import Types.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.{Position, UnitType}

import scala.collection.mutable

class Commander {
  
  var _intentions = new mutable.HashMap[FriendlyUnitInfo, Intention]
  val _nextOrderFrame = new mutable.HashMap[FriendlyUnitInfo, Int] { override def default(key: FriendlyUnitInfo): Int = 0 }
  val _lastCommands = new mutable.HashMap[FriendlyUnitInfo, String]
  var _lastIntentions = new mutable.HashMap[FriendlyUnitInfo, Intention]
  
  def intend(intention:Intention) = _intentions.put(intention.unit, intention)
  
  def onFrame() {
    val awakeIntentions = _intentions.filter(pair => _isAwake(pair._1))
    awakeIntentions.foreach(pair => pair._2.command.execute(pair._2))
    _nextOrderFrame.keySet.filterNot(_.alive).foreach(_nextOrderFrame.remove)
    _lastIntentions --= _lastIntentions.keys.filterNot(_.alive)
    awakeIntentions.foreach(pair => _lastIntentions.put(pair._1, pair._2))
    _intentions = new mutable.HashMap[FriendlyUnitInfo, Intention]
  }
  
  def attack(unit:FriendlyUnitInfo, target:UnitInfo) {
    //_recordCommand(unit, command)
    unit.baseUnit.attack(target.baseUnit)
    _sleep(unit, true)
  }
  
  def move(unit:FriendlyUnitInfo, position:Position) {
    //_recordCommand(unit, command)
    unit.baseUnit.move(position)
    _sleep(unit, false)
  }
  
  def gather(unit:FriendlyUnitInfo, resource:UnitInfo) {
    //_recordCommand(unit, command)
    unit.baseUnit.gather(resource.baseUnit)
    _sleep(unit, false)
  }
  
  def buildScarab(unit:FriendlyUnitInfo) {
    //_recordCommand(unit, command)
    unit.baseUnit.build(UnitType.Protoss_Scarab)
    _sleep(unit, false)
  }
  
  def buildInterceptor(unit:FriendlyUnitInfo) {
    //_recordCommand(unit, command)
    unit.baseUnit.build(UnitType.Protoss_Interceptor)
    _sleep(unit, false)
  }
  
  def _sleep(unit:FriendlyUnitInfo, startedAttacking:Boolean = false) {
    val baseDelay = With.game.getRemainingLatencyFrames
    val attackDelay = if (startedAttacking) unit.attackFrames else 0
    _nextOrderFrame.put(unit, baseDelay + attackDelay + With.frame)
  }
  
  def _isAwake(unit:FriendlyUnitInfo):Boolean = {
    _nextOrderFrame(unit) < With.frame
  }
  
  def _isAwake(intent:Intention):Boolean = {
    _isAwake(intent.unit)
  }
  
  def _recordCommand(unit:FriendlyUnitInfo, command:Behavior) {
    if (With.configuration.enableOverlayUnits) {
      _lastCommands.put(unit, command.getClass.getSimpleName.replace("$", ""))
    }
  }
}
