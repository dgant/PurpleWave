package Mathematics.Functions

trait Conversions {
  @inline final def fromBoolean(value: Boolean): Int = if (value) 1 else 0
  @inline final def toBoolean(value: Int): Boolean = value != 0
  @inline final def toInt(value: Boolean): Int = if (value) 1 else 0
  @inline final def toSign(value: Boolean): Int = if (value) 1 else -1

  @inline final def nanToN(value: Double, n: Double): Double = if (value.isNaN || value.isInfinity) n else value
  @inline final def nanToZero(value: Double): Double = nanToN(value, 0)
  @inline final def nanToOne(value: Double): Double = nanToN(value, 1)
  @inline final def nanToInfinity(value: Double): Double = nanToN(value, Double.PositiveInfinity)

  @inline final def signum(value: Int)        : Int = if (value < 0) -1 else if (value == 0) 0 else 1
  @inline final def signum(value: Double)     : Int = if (value < 0) -1 else if (value == 0) 0 else 1
  @inline final def forcedSignum(value: Int)  : Int = if (value < 0) -1 else 1
}
