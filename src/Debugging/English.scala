package Debugging

object English {
  def pluralize(word: String, count: Int): String = {
    if (count == 1) word else word + "s"
  }
}
