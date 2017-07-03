package Strategery.History

import Utilities.CountMap

case class ContextualHistory(
  winsByStrategy    : CountMap[String],
  lossesByStrategy  : CountMap[String])
