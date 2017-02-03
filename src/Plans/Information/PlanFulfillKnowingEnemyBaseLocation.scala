package Plans.Information

import Plans.Generic.Allocation.PlanAcquireUnitsExactly
import Plans.Plan
import Startup.With
import Strategies.PositionFinders.PositionSpecific
import Strategies.UnitMatchers.UnitMatchMobile
import Strategies.UnitPreferences.UnitPreferClose
import bwapi.Position

class PlanFulfillKnowingEnemyBaseLocation
  extends Plan {
  
  var _requireScout = new PlanAcquireUnitsExactly { setUnitMatcher(new UnitMatchMobile) }
  
  override def children(): Iterable[Plan] = {
    List(_requireScout)
  }
  
  override def onFrame() {
    _getFirstScoutingPosition.foreach(
      position => _requireScout.setUnitPreference(
        new UnitPreferClose {
          setPositionFinder(new PositionSpecific(position.toTilePosition))
        }))
    
    _requireScout.onFrame()
    _requireScout.units.foreach(_orderScout)
  }
  
  def _orderScout(scout:bwapi.Unit) {
    _getNextScoutingPosition(scout).foreach(scout.move(_))
  }
  
  def _getNextScoutingPosition(scout:bwapi.Unit):Option[Position] = {
    With.scout.unexploredStartLocations
      .toList
      .sortBy(_.getPosition.getDistance(scout.getPosition))
      .headOption
      .map(_.getPosition)
  }
  
  def _getFirstScoutingPosition():Option[Position] = {
    With.scout.unexploredStartLocations
      .headOption
      .map(_.getPosition)
  }
}
