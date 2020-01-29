package Strategery

object StarCraftMapMatcher {

  def clean(mapString: String): String = {
    mapString.toLowerCase.replaceAll("[^a-z]", "")
  }
}
