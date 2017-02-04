package Plans.Information

import Plans.Generic.Allocation.{LockUnits, LockUnitsExactly}
import Plans.Plan
import Startup.With
import Strategies.PositionFinders.{PositionCenter, PositionFinder, PositionSpecific}
import Strategies.UnitPreferences.{UnitPreferClose, UnitPreference}
import Traits.Property
import bwapi.Position

class PlanFulfillKnowingEnemyBaseLocation extends Plan {
  
  val me = this
  val positionFinder = new Property[PositionFinder](new PositionCenter)
  val unitPreference = new Property[UnitPreference](new UnitPreferClose  { positionFinder.inherit(me.positionFinder) })
  val unitPlan       = new Property[LockUnits]     (new LockUnitsExactly { unitPreference.inherit(me.unitPreference) })
  
  override def getChildren: Iterable[Plan] = { List(unitPlan.get) }
  
  override def onFrame() {
    _getFirstScoutingPosition.foreach(position => positionFinder.set(new PositionSpecific(position.toTilePosition)))
    unitPlan.get.onFrame()
    unitPlan.get.units.foreach(_orderScout)
  }
  
  def _orderScout(scout:bwapi.Unit) {
    _getNextScoutingPosition(scout).foreach(scout.move(_))
  }
  
  def _getFirstScoutingPosition():Option[Position] = {
    With.scout.unexploredStartLocations
      .headOption
      .map(_.getPosition)
  }
  
  def _getNextScoutingPosition(scout:bwapi.Unit):Option[Position] = {
    With.scout.unexploredStartLocations
      .toList
      .sortBy(_.getPosition.getDistance(scout.getPosition))
      .headOption
      .map(_.getPosition)
  }
}
