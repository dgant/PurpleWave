package Information.Grids.Miscellaneous

import Information.Grids.ArrayTypes.AbstractGridArray
import Mathematics.Points.{Pixel, PixelRectangle}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitTracking.UnorderedBuffer

class GridFormationSlots extends AbstractGridArray[UnorderedBuffer[(UnitClass, Pixel)]]{
  override protected val values: Array[UnorderedBuffer[(UnitClass, Pixel)]] = indices.map(x => new UnorderedBuffer[(UnitClass, Pixel)]()).toArray
  override val defaultValue: UnorderedBuffer[(UnitClass, Pixel)] = new UnorderedBuffer[(UnitClass, Pixel)]()

  val placed = new UnorderedBuffer[(UnitClass, Pixel)]()

  override def reset(): Unit = {
    placed.foreach(p => get(p._2.tile).clear())
    placed.clear()
  }

  def tryPlace(unitClass: UnitClass, pixel: Pixel): Boolean = {
    val tile = pixel.tile
    if ( ! tile.valid) return false
    if (unitClass.isFlyer) return true
    val rectangle = PixelRectangle(
      pixel.subtract(unitClass.dimensionLeft, unitClass.dimensionUp),
      pixel.add(unitClass.dimensionRight, unitClass.dimensionDown))
    val output = ! tile.adjacent9.exists(t =>
      get(t)
        .view
        .map(other =>
          PixelRectangle(
            other._2.subtract(other._1.dimensionLeft, other._1.dimensionUp),
            other._2.add(other._1.dimensionRight, other._1.dimensionDown)))
        .exists(rectangle.intersects))
    if (output) {
      placed.add((unitClass, pixel))
      get(tile).add((unitClass, pixel))
    }
    output
  }
}
