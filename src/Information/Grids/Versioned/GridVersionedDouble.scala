package Information.Grids.Versioned

import Information.Grids.ArrayTypes.AbstractGridVersionedValue

class GridVersionedDouble extends AbstractGridVersionedValue[Double] {
  override protected var values: Array[Double] = Array.fill(length)(defaultValue)
  override val defaultValue: Double = 0.0
}
