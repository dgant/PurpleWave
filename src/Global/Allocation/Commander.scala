package Global.Allocation

import Global.Information.Combat.BattleSimulation
import Startup.With
import Types.Intents.Intent
import Types.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.Enrichment.EnrichPosition._
import bwapi.UnitType

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
  
    val combatOption = With.simulator.battles.find(_.ourGroup.units.contains(unit))
  
    if (combatOption.isEmpty) {
      _journey(unit, intent)
    } else {
      val combat = combatOption.get
      val enemyMaxRange = if (combat.enemyGroup.units.nonEmpty) combat.enemyGroup.units.map(_.range).max else 0
      val distanceToEnemy = unit.position.getDistance(combat.enemyGroup.vanguard)
      val strengthRatio = (0.01 + combat.ourScore) / (0.01 + combat.enemyScore)
    
      if (unit.cloaked && combat.enemyGroup.units.forall(!_.utype.isDetector)) {
        _destroy(unit, intent, combat)
      }
      else if (strengthRatio < 1) {
        _flee(unit, intent, combat)
      }
      else {
        _kite(unit, intent, combat)
      }
    }
  }
  
  def _setOrderDelay(unit:FriendlyUnitInfo, startedAttacking:Boolean = false) {
    val baseDelay = 0
    val attackDelay = if (startedAttacking) unit.attackFrames + 8 + With.game.getRemainingLatencyFrames else 0
    _nextOrderFrame.put(unit, With.game.getFrameCount + baseDelay + attackDelay)
  }
  
  def _kite(unit:FriendlyUnitInfo, intent:Intent, combat:BattleSimulation) {
    if (unit.isMelee) {
      _destroy(unit, intent, combat)
    }
    else if (unit.cooldownRemaining > 0) {
      _flee(unit, intent, combat)
    }
    else {
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
  
  def _destroy(unit:FriendlyUnitInfo, intent:Intent, combat:BattleSimulation) {
    val target = _getTargetInRange(unit, intent, combat)
    if (target.nonEmpty) {
      if (unit.cooldownRemaining > 0) {
        unit.baseUnit.move(target.get.position)
        _setOrderDelay(unit)
      } else {
        unit.baseUnit.attack(target.get.baseUnit)
        _setOrderDelay(unit, startedAttacking = true)
      }
    }
    else {
      _journey(unit, intent)
    }
  }
  
  def _flee(unit:FriendlyUnitInfo, intent:Intent, combat:BattleSimulation) {
    val marginOfSafety = 32 * 8
    val marginOfDesperation = 32 * 6
    if (unit.position.getDistance(combat.enemyGroup.vanguard) < marginOfSafety) {
      val fleePosition = With.geography.ourHarvestingAreas
        .map(area => area.start.midpoint(area.end).toPosition)
        .headOption
        .getOrElse(With.geography.home)
      if (unit.position.getDistance(fleePosition) > marginOfDesperation) {
        unit.baseUnit.move(fleePosition)
      }
      else {
        _destroy(unit, intent, combat)
      }
      
    } else {
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
  
  def _getTargetInRange(unit:FriendlyUnitInfo, intent:Intent, combat:BattleSimulation):Option[UnitInfo] = {
    val targets = combat.enemyGroup.units
      .filter(_.position.getDistance(unit.position) <= Math.max(unit.range, 32 * 8))
      .filterNot(target => List(UnitType.Zerg_Larva, UnitType.Zerg_Egg).contains(target.utype))
    
    if (targets.isEmpty) { return None }
    Some(targets.maxBy(target => {
      val value = target.totalCost
      val health = target.totalHealth
      val fights = if(target.canFight) 5 else 1
      val isCloseBuffer = 32
      val inRangeBonus = 4
      val isCloseBonus = 2
      val baseBonus = 1
      val inRange = target.position.getDistance(unit.position) <= unit.range
      val isClose = inRange || target.position.getDistance(unit.position) <= unit.range + isCloseBuffer
      val totalRangeBonus = if(inRange) inRangeBonus else if (isClose) isCloseBonus else baseBonus
      fights * value * totalRangeBonus / Math.max(health, unit.groundDps)
    }))
  }
}
