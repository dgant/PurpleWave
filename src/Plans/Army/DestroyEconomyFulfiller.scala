package Plans.Army

import Plans.Allocation.{LockUnits, LockUnitsNobody}
import Plans.Plan
import Startup.With
import Types.{EnemyUnitInfo, Property}
import bwapi.{Position, UnitType}

import scala.collection.JavaConverters._

class DestroyEconomyFulfiller extends Plan {
  
  val fighters = new Property[LockUnits](LockUnitsNobody)
  
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
  
    val baselikePosition = With.scout.mostBaselikeEnemyBuilding.get.getPosition
    val targetPosition = With.scout.mostBaselikeEnemyBuilding.get.getPosition
    val fallbackPosition = With.ourUnits.filter(_.getType.isBuilding).map(_.getPosition).minBy(_.getDistance(targetPosition))
    
    units.foreach(unit => _issueOrder(unit, targetPosition, fallbackPosition))
  }
  
  def _canAttack(unit:bwapi.Unit):Boolean = {
    unit.getLastCommandFrame < With.game.getFrameCount - 24
  }
  
  def _issueOrder(fighter:bwapi.Unit, targetPosition:Position, fallbackPosition:Position) {
  
    val combatRange = 32 * 13 //Just longer than Siege Tank range
    val nearbyUnits = With.game.getUnitsInRadius(fighter.getPosition, combatRange).asScala
    val nearbyEnemies = nearbyUnits.filter(_.getPlayer.isEnemy(With.game.self))
    
    if (nearbyEnemies.isEmpty) {
      fighter.attack(targetPosition)
    } else {
      val nearbyAllies = nearbyUnits
        .filter(_.getPlayer.isAlly(With.game.self))
        .filter(ally => ally.getType.groundWeapon != null)
        .filter(ally => ally.getDistance(fighter) < 32 * 8)
      val ourStrength = nearbyAllies.map(_strength).sum + _strength(fighter)
      val theirStrength = With.tracker.knownEnemyUnits
        .filter(_.possiblyStillThere)
        .filter(enemy => fighter.getDistance(enemy.getPosition) < combatRange)
        .map(_strength)
        .sum
      
      val shouldFallback = ourStrength < theirStrength && ! nearbyAllies.exists(_.getType.isBuilding)

      if (shouldFallback) {
        fighter.move(fallbackPosition)
      } else if (_canAttack(fighter)) {
        val enemyTarget = _getBestTarget(fighter, nearbyEnemies)
        if (enemyTarget.isEmpty) {
          fighter.attack(targetPosition)
        } else {
          if (fighter.getLastCommand.getTarget != enemyTarget.get) {
            fighter.attack(enemyTarget.get)
          }
        }
      }
    }
  }
    
  def _getBestTarget(fighter:bwapi.Unit, targets:Iterable[bwapi.Unit]):Option[bwapi.Unit] = {
    targets.toList
      .filter(_.exists)
      .filter(_.getType.canAttack)
      .filterNot(enemy => enemy.isCloaked && ! enemy.isDetected)
      .sortBy(enemy => enemy.getHitPoints + enemy.getShields)
      .sortBy(_.getDistance(fighter) / 16)
      .headOption
  }
  
  val _easyUnits = Set(UnitType.Terran_Marine, UnitType.Protoss_Dragoon)
  val _hardUnits = Set(UnitType.Protoss_Photon_Cannon, UnitType.Terran_Vulture)
  
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
    if (unitType == UnitType.Zerg_Sunken_Colony) { return 350 }
    return (unitType.mineralPrice + unitType.gasPrice) *
      (if(_hardUnits.contains(unitType)) { 4 }
      else if (_easyUnits.contains(unitType)) { 2 }
      else 3)
  }
}
