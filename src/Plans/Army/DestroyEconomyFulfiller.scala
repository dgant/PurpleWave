package Plans.Army

import Plans.Allocation.{LockUnits, LockUnitsNobody}
import Plans.Plan
import Startup.With
import Types.{EnemyUnitInfo, Property}
import bwapi.{Position, UnitType}
import bwta.BWTA

import scala.collection.JavaConverters._
import scala.collection.mutable

class DestroyEconomyFulfiller extends Plan {
  val fighters = new Property[LockUnits](LockUnitsNobody)
  
  val _lastOrderFrame = new mutable.HashMap[bwapi.Unit, Integer]
  
  override def getChildren: Iterable[Plan] = { List(fighters.get) }
  override def onFrame() {
    
    if (With.scout.mostBaselikeEnemyBuilding.isEmpty) {
      With.logger.warn("Trying to destroy economy without knowing where to go")
      return
    }
  
    fighters.get.onFrame()
    if ( ! fighters.get.isComplete) {
      return
    }
    
    val units = fighters.get.units
    if (units.isEmpty) {
      return
    }
    
    _lastOrderFrame.keySet.diff(units).foreach(_lastOrderFrame.remove)
    units.diff(_lastOrderFrame.keySet).foreach(_lastOrderFrame.put(_, 0))
    
    if ( ! units.exists(_canOrder)) {
      return
    }
    
    val enemyHasWorkersLeft =
      With.tracker.knownEnemyUnits.filter(_.getType.isWorker).size > 0 ||
      List(UnitType.Protoss_Probe, UnitType.Terran_SCV, UnitType.Zerg_Drone).map(With.history.destroyedEnemyUnits(_)).sum < 3
  
    val baselikePosition = With.scout.mostBaselikeEnemyBuilding.get.getPosition
    val targetPosition = if (enemyHasWorkersLeft) {
      BWTA.getNearestBaseLocation(baselikePosition).getPosition
    } else {
      baselikePosition
    }
    val fallbackPosition = With.ourUnits.filter(_.getType.isBuilding).map(_.getPosition).minBy(_.getDistance(targetPosition))
    
    units
      .filter(_canOrder)
      .foreach(unit => {
        _issueOrder(unit, targetPosition, fallbackPosition)
        _lastOrderFrame(unit) = With.game.getFrameCount
      })
  }
  
  def _canOrder(unit:bwapi.Unit):Boolean = {
    _lastOrderFrame(unit) < With.game.getFrameCount - 24
  }
  
  def _issueOrder(fighter:bwapi.Unit, targetPosition:Position, fallbackPosition:Position) {
  
    val workerLineRadius = 32 * 3
    val weAreByTheirWorkerLine = fighter.getPosition.getDistance(targetPosition) < workerLineRadius
    if (weAreByTheirWorkerLine) {
      val enemyFightersNearby = With.game.getUnitsInRadius(targetPosition, workerLineRadius).asScala
        .filter(_.getPlayer.isEnemy(With.game.self))
        .filter(_.getType.canAttack)
        .filterNot(_.isFlying)
      if (enemyFightersNearby.nonEmpty) {
        val enemyTarget = getBestTarget(fighter, enemyFightersNearby)
        if (fighter.getLastCommand.getTarget != enemyTarget) {
          fighter.attack(enemyTarget)
        }
      }
      else if (With.tracker.knownEnemyUnits.nonEmpty) {
        fighter.patrol(With.tracker.knownEnemyUnits.minBy(enemy => fighter.getDistance(enemy.getPosition)).getPosition)
      }
    }
    else {
      val combatRange = 32 * 13 //Just longer than Siege Tank range
      val nearbyUnits = With.game.getUnitsInRadius(fighter.getPosition, combatRange).asScala
      val nearbyEnemies = nearbyUnits.filter(_.getPlayer.isEnemy(With.game.self))
      
      if (nearbyEnemies.isEmpty) {
        fighter.move(targetPosition)
      } else {
        val nearbyAllies = nearbyUnits
          .filter(_.getPlayer.isAlly(With.game.self))
          .filter(ally => ally.getType.groundWeapon != null)
          .filter(ally => ally.getDistance(fighter) < ally.getType.groundWeapon.maxRange + 32 * 3)
        val ourStrength = nearbyAllies.map(_strength).sum
        val theirStrength = nearbyEnemies.map(_strength).sum
        val shouldFallback = ourStrength < theirStrength && ! nearbyAllies.exists(_.getType.isBuilding)
  
        if (shouldFallback) {
          fighter.move(fallbackPosition)
        } else {
          val enemyTarget = getBestTarget(fighter, nearbyEnemies)
          if (fighter.getLastCommand.getTarget != enemyTarget) {
            fighter.attack(enemyTarget)
          }
        }
      }
    }
  }
    
  def getBestTarget(fighter:bwapi.Unit, targets:Iterable[bwapi.Unit]):bwapi.Unit = {
    targets.toList.sortBy(_.getDistance(fighter) / 16).minBy(enemy => enemy.getHitPoints + enemy.getShields)
  }
  
  val _easyUnits = Set(UnitType.Terran_Marine, UnitType.Protoss_Dragoon)
  val _scaryUnits = Set(UnitType.Protoss_Photon_Cannon, UnitType.Zerg_Sunken_Colony, UnitType.Terran_Vulture)
  
  def _strength(unit:EnemyUnitInfo):Int = {
    if ( ! unit.isCompleted) { return 0 }
    _strength(unit.getType)
  }
  
  def _strength(unit:bwapi.Unit):Int = {
    if ( ! unit.isCompleted) { return 0 }
    _strength(unit.getType)
  }
  
  def _strength(unitType:UnitType):Int = {
    if (unitType.isWorker) { return 0 }
    if ( ! unitType.canAttack && unitType != UnitType.Terran_Bunker) { return 0 }
    return (unitType.mineralPrice + unitType.gasPrice) *
      (if(_scaryUnits.contains(unitType)) { 4 }
      else if (_easyUnits.contains(unitType)) { 1 }
      else 2)
  }
}
