package Debugging

object Decimal {
  def apply(value: Double, decimals: Int = 2): String = s"%1.${decimals}f".format(value)
}
