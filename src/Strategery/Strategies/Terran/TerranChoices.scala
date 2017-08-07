package Strategery.Strategies.Terran

import Strategery.Strategies.Terran.Global._
import Strategery.Strategies._

object TerranChoices {
  
  val overall: Iterable[Strategy] = Vector(
    Proxy5RaxAllIn,
    ProxyBBS2StartLocations,
    ProxyBBS3StartLocations,
    MassMarineAllIn,
    Macro,
    MassMarineFFA)
}