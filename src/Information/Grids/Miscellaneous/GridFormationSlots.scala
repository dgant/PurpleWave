package Information.Grids.Miscellaneous

import Information.Grids.ArrayTypes.AbstractGridArray
import Mathematics.Points.{Pixel, PixelRectangle, Tile}
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitTracking.UnorderedBuffer

final class GridFormationSlots extends AbstractGridArray[UnorderedBuffer[(UnitClass, Pixel)]]{
  override protected val values: Array[UnorderedBuffer[(UnitClass, Pixel)]] = indices.map(x => new UnorderedBuffer[(UnitClass, Pixel)]()).toArray
  override val defaultValue: UnorderedBuffer[(UnitClass, Pixel)] = new UnorderedBuffer[(UnitClass, Pixel)]()

  def placed: Seq[(UnitClass, Pixel)] = _placed.view.filterNot(_._1 == blockClass)
  val _placed = new UnorderedBuffer[(UnitClass, Pixel)]()

  override def reset(): Unit = {
    _placed.foreach(p => get(p._2.tile).clear())
    _placed.clear()
  }

  private val blockClass = Protoss.FenixDragoon
  def block(tile: Tile): Unit = {
    if (tile.valid) {
      if ( ! get(tile).exists(_._1 == blockClass)) {
        forcePlaceUnchecked(blockClass, tile.center, tile)
      }
    }
  }

  def tryPlace(unitClass: UnitClass, pixel: Pixel): Boolean = {
    if ( ! pixel.valid) return false
    val tile = pixel.tile
    if (unitClass.isFlyer) return true
    val rectangle = PixelRectangle(
      pixel.subtract(unitClass.dimensionLeft, unitClass.dimensionUp),
      pixel.add(unitClass.dimensionRight, unitClass.dimensionDown))
    val canPlace = ! tile.adjacent9.exists(t =>
      get(t).exists(other => rectangle.intersects(PixelRectangle(
        other._2.subtract(other._1.dimensionLeft, other._1.dimensionUp),
        other._2.add(other._1.dimensionRight, other._1.dimensionDown)))))
    if (canPlace) forcePlaceUnchecked(unitClass, pixel, tile)
    canPlace
  }

  def forcePlaceUnchecked(unitClass: UnitClass, pixel: Pixel): Unit = {
    forcePlaceUnchecked(unitClass, pixel, pixel.tile)
  }

  def forcePlaceUnchecked(unitClass: UnitClass, pixel: Pixel, tile: Tile): Unit = {
    _placed.add((unitClass, pixel))
    get(tile).add((unitClass, pixel))
  }
}
