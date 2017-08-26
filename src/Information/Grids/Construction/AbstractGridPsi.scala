package Information.Grids.Construction

import Information.Grids.ArrayTypes.AbstractGridTimestamp
import Lifecycle.With
import Mathematics.Points.Point
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

abstract class AbstractGridPsi extends AbstractGridTimestamp {
  
  private var lastPylons:Set[FriendlyUnitInfo] = Set.empty
  
  override def updateTimestamps() {
    
    val newPylons = pylons
    if (newPylons == lastPylons) return
    lastPylons = newPylons
    
    pylons.foreach(pylon =>
      psiPoints.foreach(point => {
        val tile = pylon.tileTopLeft.add(point)
        if (tile.valid) {
          set(tile, frameUpdated)
        }
      }))
  }
  
  val psiPoints: Array[Point]
  def pylons: Set[FriendlyUnitInfo] = With.units.ours.filter(unit => unit.aliveAndComplete && unit.is(Protoss.Pylon))
}
