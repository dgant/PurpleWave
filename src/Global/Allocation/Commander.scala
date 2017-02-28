package Global.Allocation

import Global.Information.Combat.CombatSimulation
import Startup.With
import Types.Intents.Intent
import Types.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.{Position, UnitType}
import Utilities.Enrichment.EnrichPosition._

import scala.collection.mutable

class Commander {
  
  val _intents = new mutable.HashMap[FriendlyUnitInfo, Intent]
  val _nextOrderFrame = new mutable.HashMap[FriendlyUnitInfo, Int] {
    override def default(key: FriendlyUnitInfo): Int = 0 }
  
  def intend(unit:FriendlyUnitInfo, intent:Intent){
    _intents.put(unit, intent)
  }
  
  def onFrame() {
    _intents.foreach(intent => _order(intent._1, intent._2))
    _intents.clear()
  }
  
  def _order(unit:FriendlyUnitInfo, intent:Intent) {
    if (_nextOrderFrame(unit) > With.game.getFrameCount) {
      return
    }
  
    val combatOption = With.simulator.combats.find(_.ourGroup.units.contains(unit))
  
    if (combatOption.isEmpty) {
      _journey(unit, intent)
    } else {
      val combat = combatOption.get
      val enemyMaxRange = combat.enemyGroup.units.map(_.range).max
      val distanceToEnemy = unit.position.getDistance(combat.enemyGroup.vanguard)
      val strengthRatio = (1.0 + combat.ourScore) / (1.0 + combat.enemyScore)
    
      if (unit.cloaked && combat.enemyGroup.units.forall(!_.unitType.isDetector)) {
        _destroy(unit, intent, combat)
      }
      else if (strengthRatio < .75 && distanceToEnemy < 32 + enemyMaxRange) {
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
  
  def _setOrderDelay(unit:FriendlyUnitInfo, startedAttacking:Boolean = false) {
    val baseDelay = 7 + With.game.getRemainingLatencyFrames
    val attackDelay = if (startedAttacking) unit.attackFrames else 0
    _nextOrderFrame.put(unit, With.game.getFrameCount + baseDelay + attackDelay)
  }
  
  def _kite(unit:FriendlyUnitInfo, intent:Intent, combat:CombatSimulation) {
    if (unit.cooldownRemaining > 0 && unit.range > 32) {
      _flee(unit, intent, combat)
    } else {
      val target = _getTargetInRange(unit, intent, combat)
      if (target.isEmpty) {
        unit.baseUnit.patrol(intent.position.get)
        _setOrderDelay(unit, startedAttacking = true)
      } else {
        unit.baseUnit.attack(target.get.baseUnit)
        _setOrderDelay(unit, startedAttacking = true)
      }
    }
  }
  
  def _destroy(unit:FriendlyUnitInfo, intent:Intent, combat:CombatSimulation) {
    if (unit.cooldownRemaining > 0) {
      unit.baseUnit.move(intent.position.get)
      _setOrderDelay(unit)
    }
    else {
      val target = _getTargetInRange(unit, intent, combat)
      if (target.isEmpty) {
        _journey(unit, intent)
      } else {
        unit.baseUnit.attack(target.get.baseUnit)
        _setOrderDelay(unit, startedAttacking = true)
      }
    }
  }
  
  def _flee(unit:FriendlyUnitInfo, intent:Intent, combat:CombatSimulation) {
    val leash = 32 * 5
    val fleeDistance = 32 * 3
    var fleePosition = With.geography.ourHarvestingAreas
      .map(area => area.start.midpoint(area.end).toPosition)
      .headOption
      .getOrElse(With.geography.home.position)
    val dx = unit.x - combat.enemyGroup.vanguard.getX
    val dy = unit.y - combat.enemyGroup.vanguard.getY
    val lengthSquared = dx*dx + dy*dy
    if (lengthSquared > 0) {
      val directlyAway = new Position(
        (unit.x + fleeDistance * dx / Math.sqrt(lengthSquared)).toInt,
        (unit.y + fleeDistance * dy / Math.sqrt(lengthSquared)).toInt)
      if (directlyAway.isValid) {
       // fleePosition = directlyAway
      }
    }
    if (fleePosition.getDistance(unit.position) > leash) {
      unit.baseUnit.move(fleePosition)
    } else {
      unit.baseUnit.patrol(fleePosition)
      _destroy(unit, intent, combat)
    }
  }
  
  def _journey(unit:FriendlyUnitInfo, intent:Intent) {
    if (unit.position.getDistance(intent.position.get) < 32 * 8) {
      unit.baseUnit.patrol(intent.position.get)
      _setOrderDelay(unit, startedAttacking = true)
    } else {
      unit.baseUnit.move(intent.position.get)
    }
  }
  
  def _getTargetInRange(unit:FriendlyUnitInfo, intent:Intent, combat:CombatSimulation):Option[UnitInfo] = {
    val targets = combat.enemyGroup.units
      .filter(_.position.getDistance(unit.position) <= Math.max(unit.range, 32 * 6))
      .filterNot(target => List(UnitType.Zerg_Larva, UnitType.Zerg_Egg).contains(target.unitType))
    
    if (targets.isEmpty) { return None }
    Some(targets.minBy(target => {
      val value = target.totalCost
      val health = target.totalHealth
      val inRangeBuffer = 16
      val inRangeBonus = 2
      val inRange = target.position.getDistance(unit.position) < unit.range + inRangeBuffer
      value * health * (if(inRange) inRangeBonus else 1)
    }))
  }
}
