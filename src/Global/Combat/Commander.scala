package Global.Combat

import Global.Combat.Commands.Command
import Startup.With
import Types.Intents.Intention
import Types.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.Position

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
  
  def _sleep(unit:FriendlyUnitInfo, startedAttacking:Boolean = false) {
    val baseDelay = With.game.getRemainingLatencyFrames
    val attackDelay = if (startedAttacking) unit.attackFrames else 0
    _nextOrderFrame.put(unit, baseDelay + attackDelay + With.game.getFrameCount)
  }
  
  def _isAwake(unit:FriendlyUnitInfo):Boolean = {
    _nextOrderFrame(unit) < With.game.getFrameCount
  }
  
  def _isAwake(intent:Intention):Boolean = {
    _isAwake(intent.unit)
  }
  
  def _recordCommand(unit:FriendlyUnitInfo, command:Command) {
    if (With.configuration.enableOverlayUnits) {
      _lastCommands.put(unit, command.getClass.getSimpleName.replace("$", ""))
    }
  }
}
