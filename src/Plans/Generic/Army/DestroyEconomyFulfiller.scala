package Plans.Generic.Army

import Plans.Generic.Allocation.{LockUnits, LockUnitsNobody}
import Plans.Plan
import Startup.With
import Types.Property
import bwapi.Position

import scala.collection.JavaConverters._
import scala.collection.mutable

class DestroyEconomyFulfiller extends Plan {
  val fighters = new Property[LockUnits](LockUnitsNobody)
  
  val _lastOrderFrame = new mutable.HashMap[bwapi.Unit, Integer]
  
  override def getChildren: Iterable[Plan] = { List(fighters.get) }
  override def onFrame() {
    
    if (With.scout.nextEnemyBase.isEmpty) {
      With.logger.warn("Trying to destroy economy without knowing where to go")
      return
    }
  
    fighters.get.onFrame()
    if ( ! fighters.get.isComplete) {
      return
    }
    
    val units = fighters.get.units
    _lastOrderFrame.keySet.diff(units).foreach(_lastOrderFrame.remove)
    units.diff(_lastOrderFrame.keySet).foreach(_lastOrderFrame.put(_, 0))
    
    val targetPosition = With.scout.nextEnemyBase.get.getPosition
    
    units
      .filter(_canOrder)
      .foreach(unit => {
        _issueOrder(unit, targetPosition)
        _lastOrderFrame(unit) = With.game.getFrameCount
      })
    
  }
  
  def _canOrder(unit:bwapi.Unit):Boolean = {
    _lastOrderFrame(unit) < With.game.getFrameCount - 24
  }
  
  def _issueOrder(fighter:bwapi.Unit, targetPosition:Position) {
    //Kill any nearby workers
    //Kill any very nearby combat units
    //Otherwise, check out the mineral line and make sure it's empty
    //Then destroy the base

    //Attack nearby targets
    //Otherwise, attack-move the mineral line
    
    val baseRadius = 32 * 25
    val combatRadius = 32 * 4
    
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
      else if (With.scout.enemyUnits.nonEmpty) {
        fighter.patrol(With.scout.enemyUnits.minBy(enemy => fighter.getDistance(enemy.getPosition)).getPosition)
      }
    }
    else {
      fighter.move(targetPosition)
    }
  }
}
