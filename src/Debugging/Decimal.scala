package Debugging

object Decimal {
  def apply(value: Double, decimals: Int): String = ("%1." + decimals + "f").format(value)
}
