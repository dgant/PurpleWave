package Strategery.Strategies.Terran

import Strategery.Strategies.Terran.Global._
import Strategery.Strategies._

object TerranChoices {
  
  val overall: Iterable[Strategy] = Vector(
    Proxy5RaxAllIn,
    Proxy8FactllIn,
    ProxyBBS2StartLocations,
    ProxyBBS3StartLocations,
    MassMarineAllIn,
    Macro)
}