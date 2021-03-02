package Debugging

object Decap {
  def apply(value: Object): String = {
    val string = value.toString
    if (string.isEmpty) return string
    f"${string.head.toLower}${string.drop(1)}"
  }
}
