package Global.Combat

import Global.Combat.Commands.Command
import Startup.With
import Types.Intents.Intention
import Types.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.{Position, TilePosition}
import Utilities.Enrichment.EnrichPosition._

import scala.collection.mutable

class Commander {
  
  var _intentions = new mutable.HashMap[FriendlyUnitInfo, Intention]
  val _nextOrderFrame = new mutable.HashMap[FriendlyUnitInfo, Int] { override def default(key: FriendlyUnitInfo): Int = 0 }
  val _lastCommands = new mutable.HashMap[FriendlyUnitInfo, String]
  var _lastIntentions = new mutable.HashMap[FriendlyUnitInfo, Intention]
  
  def intend(intention:Intention) = _intentions.put(intention.unit, intention)
  
  def onFrame() {
    _intentions.filter(pair => _isAwake(pair._1)).foreach(pair => pair._2.command.execute(pair._2))
    _nextOrderFrame.keySet.filterNot(_.alive).foreach(_nextOrderFrame.remove)
    _lastIntentions = _intentions
    _intentions = new mutable.HashMap[FriendlyUnitInfo, Intention]
  }
  
  def attack(command:Command, unit:FriendlyUnitInfo, target:UnitInfo) {
    _recordCommand(unit, command)
    if (target.visible) {
      unit.baseUnit.attack(target.baseUnit)
      _sleep(unit, true)
    } else {
      attack(command, unit, target.position)
    }
  }
  
  def attack(command:Command, unit:FriendlyUnitInfo, tilePosition:TilePosition) {
    attack(command, unit, tilePosition.toPosition)
  }
  
  def attack(command:Command, unit:FriendlyUnitInfo, position:Position) {
    _recordCommand(unit, command)
    if (With.game.isVisible(position.toTilePosition)) {
      unit.baseUnit.patrol(position)
    }
    else {
      unit.baseUnit.attack(position)
    }
    _sleep(unit, true)
  }
  
  def move(command:Command, unit:FriendlyUnitInfo, position:Position) {
    _recordCommand(unit, command)
    unit.baseUnit.move(position)
    _sleep(unit, false)
  }
  
  def move(command:Command, unit:FriendlyUnitInfo, tilePosition:TilePosition) {
    move(command, unit, tilePosition.centerPixel)
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
