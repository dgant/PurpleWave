package Processes

import Startup.With
import scala.collection.JavaConverters._

class Economist {
  
  def mineralsPerMinute:Float = {
    With.map.ourMiningAreas
      .flatten(miningArea => With.game.getUnitsInRectangle(
        miningArea.start.toPosition,
        miningArea.end.toPosition).asScala)
      //.filter(_.getOrder == bwapi.Or)
    
    3.3f
  }
}
