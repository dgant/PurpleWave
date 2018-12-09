package Information.Grids.Lambda

import Information.Grids.ArrayTypes.AbstractGridArray

abstract class AbstractGridFixedLambda[T](default: T, initializer: (Int) => T) extends AbstractGridArray[T] {
  override val defaultValue: T = default
}
