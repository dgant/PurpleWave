package Debugging.Visualization.Data

import ProxyBwapi.UnitInfo.UnitInfo
import Startup.With

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class MovementHeuristicViews {
  
  private val views = new mutable.HashMap[UnitInfo, ListBuffer[MovementHeuristicView]]
  
  def reset(unit:UnitInfo) {
    if (enabled) views.get(unit).foreach(_.clear())
  }
  
  def add(view:MovementHeuristicView) {
    
    if (enabled) {
      if ( !views.contains(view.intent.unit)) {
        views.put(view.intent.unit, new ListBuffer)
      }
      
      views(view.intent.unit).append(view)
    }
  }
  
  def get:Iterable[Iterable[MovementHeuristicView]] = views.values
  
  def cleanup() = views.keys.filterNot(_.alive).foreach(views.remove)
  
  def enabled:Boolean = With.configuration.enableVisualization && With.configuration.enableVisualizationMovementHeuristics
}
