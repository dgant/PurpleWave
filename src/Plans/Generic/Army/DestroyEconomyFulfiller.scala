package Plans.Generic.Army

import Development.Logger
import Plans.Generic.Allocation.{LockUnits, LockUnitsNobody}
import Plans.Plan
import Startup.With
import Traits.Property

import scala.collection.mutable

class DestroyEconomyFulfiller extends Plan {
  val fighters = new Property[LockUnits](LockUnitsNobody)
  
  val _lastOrderFrame = new mutable.HashMap[bwapi.Unit, Integer]
  
  override def getChildren: Iterable[Plan] = { List(fighters.get) }
  override def onFrame() {
    if (With.scout.enemyBaseLocationPosition.isEmpty) {
      Logger.warn("Trying to destroy economy without knowing where to go")
    }
  
    fighters.get.onFrame()
    if ( ! fighters.get.isComplete) {
      return
    }
    
    val units = fighters.get.units
    _lastOrderFrame.keySet.diff(units).foreach(_lastOrderFrame.remove)
    units.diff(_lastOrderFrame.keySet).foreach(_lastOrderFrame.put(_, 0))
    
    units
      .filter(_canOrder)
      .foreach(unit => {
        unit.attack(With.scout.enemyBaseLocationPosition.get)
        _lastOrderFrame(unit) = With.game.getFrameCount
      })
    
  }
  
  def _canOrder(unit:bwapi.Unit):Boolean = {
    _lastOrderFrame(unit) < With.game.getFrameCount - 12
  }
}
