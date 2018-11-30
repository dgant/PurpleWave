package Information.Grids.Construction

import Information.Grids.ArrayTypes.AbstractGridTimestamp
import Lifecycle.With
import Mathematics.Points.Point
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

abstract class AbstractGridPsi extends AbstractGridTimestamp {
  
  private var lastPylons: Set[FriendlyUnitInfo] = Set.empty
  
  override def needsUpdate: Boolean = {
    val newPylons = pylons
    val output = newPylons == lastPylons
    lastPylons = newPylons
    output
  }
  
  private val wrapThreshold = 18
  override def updateTimestamps() {
    pylons.foreach(pylon => {
      val pylonTile = pylon.tileIncludingCenter
      psiPoints.foreach(point => {
        val tile = pylon.tileTopLeft.add(point)
        if (tile.valid
          && Math.abs(tile.x - pylonTile.x) < wrapThreshold
          && Math.abs(tile.y - pylonTile.y) < wrapThreshold) {
          stamp(tile)
        }
      })})
  }
  
  val psiPoints: Array[Point]
  def pylons: Set[FriendlyUnitInfo] = With.units.ours.filter(unit => unit.aliveAndComplete && unit.is(Protoss.Pylon))
}
