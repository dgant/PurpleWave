package Global.Allocation

import Global.Allocation.Intents.Intent
import Global.Information.Combat.CombatSimulation
import Startup.With
import Utilities.Enrichment.EnrichUnit._
import bwapi.{Position, Unit}

import scala.collection.mutable

class Commander {
  
  val _intents = new mutable.HashMap[bwapi.Unit, Intent]
  val _nextOrderFrame = new mutable.HashMap[bwapi.Unit, Int] { override def default(key: Unit): Int = 0 }
  
  def intend(unit:bwapi.Unit, intent:Intent){
    _intents.put(unit, intent)
  }
  
  def onFrame() {
    _intents.foreach(intent => _order(intent._1, intent._2))
    _intents.clear()
  }
  
  def _order(unit:bwapi.Unit, intent:Intent) {
    if (_nextOrderFrame(unit) > With.game.getFrameCount) {
      return
    }
  
    val combatOption = With.simulator.combats.find(_.ourGroup.units.contains(unit))
  
    if (combatOption.isEmpty) {
      _journey(unit, intent)
    } else {
      val combat = combatOption.get
      val strengthRatio = (1.0 + combat.ourScore) / (1.0 + combat.enemyScore)
    
      if (unit.isCloaked && combat.enemyGroup.units.forall(!_.getType.isDetector)) {
        _destroy(unit, intent, combat)
      }
      else if (strengthRatio < .75) {
        _flee(unit, intent, combat)
      }
      else if (strengthRatio > 1.3) {
        _destroy(unit, intent, combat)
      }
      else {
        _kite(unit, intent, combat)
      }
    }
  }
  
  def _setOrderDelay(unit:bwapi.Unit, startedAttacking:Boolean = false) {
    val baseDelay = 7 + With.game.getRemainingLatencyFrames
    val attackDelay = if (startedAttacking) unit.attackFrames else 0
    _nextOrderFrame.put(unit, With.game.getFrameCount + baseDelay + attackDelay)
  }
  
  def _kite(unit:bwapi.Unit, intent:Intent, combat:CombatSimulation) {
    if (unit.cooldownRemaining > 0) {
      _flee(unit, intent, combat)
    } else {
      val target = _getTargetInRange(unit, intent, combat)
      if (target.isEmpty) {
        unit.patrol(intent.position.get)
        _setOrderDelay(unit, startedAttacking = true)
      } else {
        unit.attack(target.get)
        _setOrderDelay(unit, startedAttacking = true)
      }
    }
  }
  
  def _destroy(unit:bwapi.Unit, intent:Intent, combat:CombatSimulation) {
    if (unit.cooldownRemaining > 0) {
      unit.move(intent.position.get)
      _setOrderDelay(unit)
    }
    else {
      val target = _getTargetInRange(unit, intent, combat)
      if (target.isEmpty) {
        _journey(unit, intent)
      } else {
        unit.attack(target.get)
        _setOrderDelay(unit, startedAttacking = true)
      }
    }
  }
  
  def _flee(unit:bwapi.Unit, intent:Intent, combat:CombatSimulation) {
    val fleeDistance = 32 * 3
    var fleePosition = With.geography.home.getPosition
    val dx = unit.getX - combat.enemyGroup.vanguard.getX
    val dy = unit.getY - combat.enemyGroup.vanguard.getY
    val lengthSquared = dx*dx + dy*dy
    if (lengthSquared > 0) {
      val directlyAway = new Position(
        (unit.getX + fleeDistance * dx / Math.sqrt(lengthSquared)).toInt,
        (unit.getY + fleeDistance * dy / Math.sqrt(lengthSquared)).toInt)
      if (directlyAway.isValid) {
       // fleePosition = directlyAway
      }
    }
    unit.move(fleePosition)
  }
  
  def _journey(unit:bwapi.Unit, intent:Intent) {
    if (unit.getDistance(intent.position.get) < 32 * 8) {
      unit.patrol(intent.position.get)
      _setOrderDelay(unit, startedAttacking = true)
    } else {
      unit.move(intent.position.get)
    }
  }
  
  def _getTargetInRange(unit:bwapi.Unit, intent:Intent, combat:CombatSimulation):Option[bwapi.Unit] = {
    val targets = combat.enemyGroup.units.filter(_.getDistance(unit) <= Math.max(unit.range, 64))
    if (targets.isEmpty) { return None }
    Some(targets.minBy(_.totalHealth))
  }
}
