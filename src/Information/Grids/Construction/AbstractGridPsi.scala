package Information.Grids.Construction

import Information.Grids.ArrayTypes.AbstractGridFramestamp
import Lifecycle.With
import Mathematics.Points.Point
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

abstract class AbstractGridPsi extends AbstractGridFramestamp {

  val psiPoints: Array[Point]

  private var lastPylons: Vector[FriendlyUnitInfo] = Vector.empty

  private val wrapThreshold = 18
  override def update(): Unit = {
    val newPylons = With.units.ours.view.filter(unit => unit.aliveAndComplete && unit.is(Protoss.Pylon)).toVector
    if (newPylons != lastPylons) {
      updateVersion()
      newPylons.foreach(pylon => {
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
    lastPylons = newPylons
  }
}
