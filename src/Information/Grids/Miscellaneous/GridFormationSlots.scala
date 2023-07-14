package Information.Grids.Miscellaneous

import Information.Grids.ArrayTypes.AbstractGridArray
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, Tile}
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

  private def intersects(x0: Int, y0: Int, x1: Int, y1: Int, i: Int): Boolean = {
    val j = Maff.clamp(i, 0, lengthMinusOne)
    val slots = get(j)
    var k = 0
    while (k < slots.length) {
      val (u, p) = slots(k)
      if (Maff.rectanglesIntersect(x0, y0, x1, y1,
        p.x - u.dimensionLeft,
        p.y - u.dimensionUp,
        p.x + u.dimensionRight,
        p.y + u.dimensionDown)) {
        return true
      }
      k += 1
    }
    false
  }
  def tryPlace(unitClass: UnitClass, pixel: Pixel): Boolean = {
    val i = this.i(Maff.div32(pixel.x), Maff.div32(pixel.y))
    if (i < 0 || i > length) return false
    if (unitClass.isFlyer) return true
    val x0 = pixel.x - unitClass.dimensionLeft  - 2
    val x1 = pixel.x + unitClass.dimensionRight + 2
    val y0 = pixel.y - unitClass.dimensionUp    - 2
    val y1 = pixel.y + unitClass.dimensionDown  + 2
    val output = (
         ! intersects(x0, y0, x1, y1, i - 1)
      && ! intersects(x0, y0, x1, y1, i)
      && ! intersects(x0, y0, x1, y1, i + 1)
      && ! intersects(x0, y0, x1, y1, i - 1 - With.mapTileWidth)
      && ! intersects(x0, y0, x1, y1, i     - With.mapTileWidth)
      && ! intersects(x0, y0, x1, y1, i + 1 - With.mapTileWidth)
      && ! intersects(x0, y0, x1, y1, i - 1 + With.mapTileWidth)
      && ! intersects(x0, y0, x1, y1, i     + With.mapTileWidth)
      && ! intersects(x0, y0, x1, y1, i + 1 + With.mapTileWidth))
    if (output) forcePlaceUnchecked(unitClass, pixel)
    output
  }

  def forcePlaceUnchecked(unitClass: UnitClass, pixel: Pixel): Unit = {
    forcePlaceUnchecked(unitClass, pixel, pixel.tile)
  }

  def forcePlaceUnchecked(unitClass: UnitClass, pixel: Pixel, tile: Tile): Unit = {
    _placed.add((unitClass, pixel))
    get(tile).add((unitClass, pixel))
  }
}
