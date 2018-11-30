package Information.Grids.ArrayTypes
import Mathematics.Points.Tile
import Performance.Cache

class AbstractCacheArray[T](initializer: Int => Cache[T]) extends AbstractGridArray[Cache[T]] {
  override protected var values: Array[Cache[T]] = indices.map(initializer).toArray
  override def defaultValue: Cache[T] = initializer(0)
  def apply(i: Int): T = get(i)()
  def apply(tile: Tile): T = get(tile)()
}