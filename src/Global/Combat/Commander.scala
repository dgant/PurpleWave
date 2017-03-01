package Global.Combat

import Startup.With
import Types.Intents.Intention
import Types.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.Enrichment.EnrichPosition._
import bwapi.UnitType

import scala.collection.mutable

class Commander {
  
  val _intentions = new mutable.HashSet[Intention]
  val _nextOrderFrame = new mutable.HashMap[FriendlyUnitInfo, Int] { override def default(key: FriendlyUnitInfo): Int = 0 }
  
  def intend(intention:Intention) { _intentions.add(intention) }
  
  def onFrame() {
    _intentions.foreach(_fulfill)
    _intentions.clear()
    _nextOrderFrame.keySet.filter(_.alive).foreach(_nextOrderFrame.remove)
  }
  
  def setOrderDelay(unit:FriendlyUnitInfo, startedAttacking:Boolean = false) {
    val delay = if (startedAttacking) unit.attackFrames + 8 + With.game.getRemainingLatencyFrames else 0
    _nextOrderFrame.put(unit, delay + With.game.getFrameCount)
  }
  
  def _fulfill(intent:Intention) {
    val unit = intent.unit
    if (_nextOrderFrame(unit) > With.game.getFrameCount) return
  
    val battleOption = With.battles.battles.find(_.us.units.contains(unit))
  
    if (battleOption.isEmpty || battleOption.get.enemy.units.isEmpty) {
      _travel(intent)
    } else {
      val battle = battleOption.get
      intent.battle = battle
      val enemyMaxRange = if (battle.enemy.units.nonEmpty) battle.enemy.units.map(_.range).max else 0
      val distanceToEnemy = unit.position.getDistance(battle.enemy.vanguard)
      val strengthRatio = (0.01 + battle.us.strength) / (0.01 + battle.enemy.strength)
    
      if (unit.cloaked && ! battle.enemy.units.exists(_.utype.isDetector)) {
        _fight(intent)
      }
      else if (strengthRatio < 1) {
        _flee(intent)
      }
      else {
        _fightWhileAdvancing(intent)
      }
    }
  }
  
  def _fightWhileAdvancing(intent:Intention) {
    val unit = intent.unit
    val closestEnemy = intent.battle.enemy.units.minBy(_.distanceSquared(unit))
    val closestEnemyDistance2 = unit.distanceSquared(closestEnemy)
    if (unit.onCooldown || unit.range <= closestEnemy.range) {
      val kiteRange = unit.range
      if (closestEnemyDistance2 < kiteRange * kiteRange) {
        _flee(intent)
      }
      else {
        _travel(intent)
      }
    }
    else {
      intent.targetUnit = Some(closestEnemy)
      _fight(intent)
    }
  }
  
  def _fight(intent:Intention) {
    val unit = intent.unit
    val closestEnemy = intent.battle.enemy.units.minBy(_.distanceSquared(unit))
    val closestEnemyDistance2 = intent.unit.distanceSquared(closestEnemy)
    if (unit.onCooldown) {
      unit.baseUnit.move(closestEnemy.position)
      setOrderDelay(unit)
    } else {
      unit.baseUnit.attack(closestEnemy.baseUnit)
      setOrderDelay(unit, startedAttacking = true)
    }
  }
  
  def _flee(intent:Intention) {
    val unit = intent.unit
    val marginOfSafety = 32 * 3
    val marginOfDesperation = 32 * 10
    if (intent.battle.enemy.units.exists(enemy => enemy.distance(unit) + enemy.range < marginOfSafety)) {
      val fleePosition = With.geography.ourHarvestingAreas
        .map(area => area.start.midpoint(area.end).toPosition)
        .headOption
        .getOrElse(With.geography.home)
      if (unit.distance(fleePosition) > marginOfDesperation) {
        unit.baseUnit.move(fleePosition)
        return
      }
    }
    _fight(intent)
  }
  
  def _travel(intent:Intention) {
    val unit = intent.unit
    if (unit.distance(intent.destination.get) > 32 * 8) {
      unit.baseUnit.move(intent.destination.get)
    } else {
      unit.baseUnit.patrol(intent.destination.get)
    }
    setOrderDelay(unit, startedAttacking = true)
  }
  
  def _getTargetInRange(intent:Intention):Option[UnitInfo] = {
    val unit = intent.unit
    val targets = intent.battle.enemy.units
      .filter(_.position.getDistance(unit.position) <= Math.max(unit.range, 32 * 4))
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
      val inRange = target.distance(unit) <= unit.range
      val isClose = inRange || target.distance(unit) <= unit.range + isCloseBuffer
      val totalRangeBonus = if(inRange) inRangeBonus else if (isClose) isCloseBonus else baseBonus
      fights * value * totalRangeBonus / Math.max(health, unit.groundDps)
    }))
  }
}
