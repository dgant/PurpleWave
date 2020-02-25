package Information.Grids

import Information.Grids.ArrayTypes.AbstractGridVector
import Lifecycle.With
import Mathematics.Points.Tile
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class GridUnits extends AbstractGridVector[UnitInfo] {

  val updateIntervals = new mutable.Queue[Int]
  var lastUpdateDuration: Int = 0
  var lastUpdateFrame: Int = 0
  override def update(): Unit = {
    lastUpdateDuration = With.framesSince(lastUpdateFrame)
    lastUpdateFrame = With.frame
    updateIntervals += lastUpdateDuration
    while (updateIntervals.sum > With.reaction.runtimeQueueDuration) { updateIntervals.dequeue() }
    super.update()
  }
  
  override protected def getObjects: Traversable[UnitInfo] = With.units.all
  override protected def getTile(item: UnitInfo): Tile = item.tileIncludingCenter
}
