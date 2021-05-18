package Information.Grids.Versioned

import Information.Grids.ArrayTypes.AbstractGridVersionedValue

class GridVersionedInt extends AbstractGridVersionedValue[Int] {
  override protected var values: Array[Int] = Array.fill(length)(defaultValue)
  override val defaultValue: Int = 0
}
