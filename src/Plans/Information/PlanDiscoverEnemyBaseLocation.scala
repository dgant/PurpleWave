package Plans.Information

import Plans.Generic.Allocation.PlanAcquireUnitsExactly
import Plans.Generic.Compound.PlanCompleteAllInParallel
import Startup.With
import Strategies.UnitMatchers.UnitMatchMobile

class PlanDiscoverEnemyBaseLocation extends PlanCompleteAllInParallel {
  
  var _requireScout = new PlanAcquireUnitsExactly(new UnitMatchMobile)
  kids = List(_requireScout)
  
  override def execute() {
    super.execute()
    _requireScout.units.foreach(_orderScout)
  }
  
  def _orderScout(scout:bwapi.Unit) {
    //Crude: Only think about start locations, and never explore one twice
    With.scout.unexploredStartLocations
      .toList
      .sortBy(_.getPosition.getDistance(scout.getPosition))
      .headOption
      .foreach(base => scout.move(base.getPosition))
  }
}
