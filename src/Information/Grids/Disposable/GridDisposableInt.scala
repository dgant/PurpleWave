package Information.Grids.Disposable

import Information.Grids.ArrayTypes.AbstractGridVersionedValue

class GridDisposableInt extends AbstractGridVersionedValue[Int] {
  override protected var values: Array[Int] = Array.fill(length)(defaultValue)
  override def defaultValue: Int = 0
}
