package Debugging.Visualization.Data

import ProxyBwapi.UnitInfo.UnitInfo
import Startup.With

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class MovementHeuristicViews {
  
  private val viewsByUnitId = new mutable.HashMap[Int, ListBuffer[MovementHeuristicView]]
  
  def reset(unit:UnitInfo) {
    if (enabled) viewsByUnitId.remove(unit.id)
  }
  
  def add(view:MovementHeuristicView) {
    if (enabled) {
      if ( ! viewsByUnitId.contains(view.intent.unit.id)) {
        viewsByUnitId.put(view.intent.unit.id, new ListBuffer[MovementHeuristicView])
      }
      
      viewsByUnitId(view.intent.unit.id).append(view)
    }
  }
  
  def get:Iterable[ListBuffer[MovementHeuristicView]] = viewsByUnitId.values
  
  def cleanup() = {
    viewsByUnitId.keys.filterNot(id => With.units.getId(id).exists(_.selected)).foreach(viewsByUnitId.remove)
  }
  
  def enabled:Boolean = With.configuration.enableVisualization && With.configuration.enableVisualizationMovementHeuristics
}
