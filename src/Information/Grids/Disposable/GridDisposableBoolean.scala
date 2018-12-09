package Information.Grids.Disposable

import Information.Grids.ArrayTypes.AbstractGridVersionedValue

class GridDisposableBoolean extends AbstractGridVersionedValue[Boolean] {
  override protected var values: Array[Boolean] = Array.fill(length)(defaultValue)
  override def defaultValue: Boolean = false
}
