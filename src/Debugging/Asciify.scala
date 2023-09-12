package Debugging

object Asciify {
  def apply(v: String): String = v.replaceAll("[^\\x00-\\x7F]", "")
}
