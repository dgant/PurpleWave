package Information.Grids.Versioned

import Information.Grids.ArrayTypes.AbstractGridVersionedValue

class GridVersionedDouble extends AbstractGridVersionedValue[Double] {
  override val defaultValue: Double = 0.0
  override protected val values: Array[Double] = Array.fill(length)(defaultValue)
}
