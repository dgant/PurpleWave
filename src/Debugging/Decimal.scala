package Debugging

object Decimal {
  def apply(value: Double, decimals: Int): String = s"%1.${decimals}f".format(value)
}
