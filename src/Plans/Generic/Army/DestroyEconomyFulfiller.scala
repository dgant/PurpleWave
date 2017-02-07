package Plans.Generic.Army

import Development.Logger
import Plans.Generic.Allocation.{LockUnits, LockUnitsNobody}
import Plans.Plan
import Startup.With
import Traits.Property
import bwapi.Position

import scala.collection.mutable
import scala.collection.JavaConverters._

class DestroyEconomyFulfiller extends Plan {
  val fighters = new Property[LockUnits](LockUnitsNobody)
  
  val _lastOrderFrame = new mutable.HashMap[bwapi.Unit, Integer]
  
  override def getChildren: Iterable[Plan] = { List(fighters.get) }
  override def onFrame() {
    if (With.scout.enemyBaseLocationPosition.isEmpty) {
      Logger.warn("Trying to destroy economy without knowing where to go")
      return
    }
  
    fighters.get.onFrame()
    if ( ! fighters.get.isComplete) {
      return
    }
    
    val units = fighters.get.units
    _lastOrderFrame.keySet.diff(units).foreach(_lastOrderFrame.remove)
    units.diff(_lastOrderFrame.keySet).foreach(_lastOrderFrame.put(_, 0))
    
    val baseMinerals = With.game
      .getStaticMinerals
      .asScala
      .filter(_.getPosition.getApproxDistance(With.scout.enemyBaseLocationPosition.get) < 32 * 8)
    
    var targetPosition = With.scout.enemyBaseLocationPosition.get
    if (baseMinerals.nonEmpty) {
      targetPosition = new Position(
        baseMinerals.map(_.getX).sum / baseMinerals.size,
        baseMinerals.map(_.getY).sum / baseMinerals.size)
    }
    
    units
      .filter(_canOrder)
      .foreach(unit => {
        _issueOrder(unit, targetPosition)
        _lastOrderFrame(unit) = With.game.getFrameCount
      })
    
  }
  
  def _canOrder(unit:bwapi.Unit):Boolean = {
    _lastOrderFrame(unit) < With.game.getFrameCount - 12
  }
  
  def _issueOrder(unit:bwapi.Unit, targetPosition:Position) {
    //Attack nearby targets
    //Otherwise, attack-move the mineral line
    var target = With.game
      .getUnitsInRadius(
        unit.getPosition,
        unit.getType.groundWeapon.maxRange * 2)
      .asScala
      .filter(_.getType.canAttack)
      .sortBy(_.getType.isWorker)
      .headOption
    
    if (target.isDefined) {
      unit.attack(target.get)
    } else {
      unit.attack(targetPosition)
    }
  }
}
