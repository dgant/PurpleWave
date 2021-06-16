package Information.Grids.ArrayTypes

import Information.Grids.AbstractTypedGrid
import Mathematics.Points.Tile
import ProxyBwapi.UnitTracking.UnorderedBuffer

class GridBuffer[T >: Null] extends AbstractTypedGrid[UnorderedBuffer[T]] {

  final override val defaultValue: UnorderedBuffer[T] = new UnorderedBuffer[T]()
  override def repr(value: UnorderedBuffer[T]): String = value.size.toString

  final private val values: Array[UnorderedBuffer[T]] = Array.fill(length)(new UnorderedBuffer[T](8))
  final private val populatedTiles = new UnorderedBuffer[Integer]() // Using Integer here because it's nullable, a requirement for UnorderedBuffer

  @inline final def addItem(item: T, tile: Tile): Unit = {
    if (tile.valid) {
      addItemUnchecked(item, tile.i)
    }
  }

  @inline final def addItem(item: T, tileI: Int): Unit = {
    if (tileI >= 0 && tileI < values.length) {
      addItemUnchecked(item, tileI)
    }
  }

  @inline final def addItemUnchecked(item: T, tileI: Int): Unit = {
      populatedTiles.add(tileI)
      values(tileI).add(item)
  }
  
  override def update() {
    populatedTiles.foreach(values(_).clear())
    populatedTiles.clear()
  }

  override def getUnchecked(i: Int): UnorderedBuffer[T] = values(i)
}
