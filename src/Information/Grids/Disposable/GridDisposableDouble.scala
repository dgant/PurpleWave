package Information.Grids.Disposable

import Information.Grids.ArrayTypes.AbstractGridVersionedValue

class GridDisposableDouble extends AbstractGridVersionedValue[Double] {
  override protected var values: Array[Double] = Array.fill(length)(defaultValue)
  override def defaultValue: Double = 0.0
}
