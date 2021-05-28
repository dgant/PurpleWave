package Information.Grids.Versioned

import Information.Grids.ArrayTypes.AbstractGridVersionedValue

class GridVersionedInt extends AbstractGridVersionedValue[Int] {
  override val defaultValue: Int = 0
  override protected val values: Array[Int] = Array.fill(length)(defaultValue)
}
