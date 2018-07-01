package Strategery.Selection

case class Opponent(name: String, policy: StrategySelectionPolicy = StrategySelectionGreedy) {
  def matches(otherName: String): Boolean = {
    name == otherName
  }
  def matchesLoosely(otherName: String): Boolean = {
    val n1 = withoutWhitespace(name.toLowerCase)
    val n2 = withoutWhitespace(otherName.toLowerCase)
    n1 == n2
  }
  def matchesVeryLoosely(otherName: String): Boolean = {
    val n1 = withoutWhitespace(name.toLowerCase)
    val n2 = withoutWhitespace(otherName.toLowerCase)
    n1.contains(n2) || n2.contains(n1)
  }

  protected def withoutWhitespace(value: String): String = value.replaceAll(" ", "")
}
