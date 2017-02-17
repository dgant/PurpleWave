package Plans.Generic.Army

import Plans.Generic.Allocation.{LockUnits, LockUnitsNobody}
import Plans.Plan
import Startup.With
import Types.{EnemyUnitInfo, Property}
import bwapi.{Position, UnitType}

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
    
    val centroid = new Position(
      units.map(_.getPosition.getX).sum / units.size,
      units.map(_.getPosition.getY).sum / units.size)
    val targetPosition = With.scout.mostBaselikeEnemyBuilding.get.getPosition
    
    val ourStrength = units.filter(_.getDistance(centroid) < 32 * 8).map(_strength).sum
    val theirStrength = With.tracker.knownEnemyUnits.map(_strength).sum
    val shouldGather = units.size < 12 && ourStrength < theirStrength
    
    units
      .filter(_canOrder)
      .foreach(unit => {
        _issueOrder(unit, targetPosition, shouldGather, centroid)
        _lastOrderFrame(unit) = With.game.getFrameCount
      })
  }
  
  def _canOrder(unit:bwapi.Unit):Boolean = {
    _lastOrderFrame(unit) < With.game.getFrameCount - 24
  }
  
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
    if ( ! unitType.canAttack) { return 0 }
    return (unitType.mineralPrice + unitType.gasPrice) * (if(_scaryUnits.contains(unitType)) 2 else 1)
  }
  
  def _issueOrder(fighter:bwapi.Unit, targetPosition:Position, shouldGather:Boolean, gatheringPosition:Position) {
    val baseRadius = 32 * 8
    val combatRadius = fighter.getType.groundWeapon.maxRange + 32 * 2
    
    val weAreNearTheirBase = fighter.getPosition.getDistance(targetPosition) < baseRadius
    val workersNearTheirBase = With.game.getUnitsInRadius(targetPosition, baseRadius).asScala
        .filter(_.getPlayer.isEnemy(With.game.self))
        .filter(_.getType.isWorker)
    
    val enemyFightersNearby = fighter.getUnitsInRadius(combatRadius).asScala
      .filter(_.getPlayer.isEnemy(With.game.self))
      .filter(_.getType.canAttack)
      .filterNot(_.isFlying)
  
    if (enemyFightersNearby.nonEmpty) {
      fighter.attack(enemyFightersNearby.sortBy(_.getDistance(fighter)).head)
    }
    else if (weAreNearTheirBase) {
      if (workersNearTheirBase.nonEmpty) {
        fighter.attack(workersNearTheirBase.sortBy(_.getDistance(fighter)).head)
      }
      else if (With.tracker.knownEnemyUnits.exists(! _.getType.isFlyer)) {
        fighter.patrol(With.tracker.knownEnemyUnits.minBy(enemy => fighter.getDistance(enemy.getPosition)).getPosition)
      }
    }
    else {
      if (shouldGather) {
        fighter.attack(gatheringPosition)
      }
      else {
        fighter.move(targetPosition)
      }
    }
  }
}
