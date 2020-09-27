package Strategery.Strategies.Protoss

import Strategery.Strategies.Strategy

object PvEStormYes extends Strategy
object PvEStormNo extends Strategy {
  override def allowedVsHuman: Boolean = false
}

object PvEStormOptions {
  def apply() = Vector(PvEStormYes, PvEStormNo)
}