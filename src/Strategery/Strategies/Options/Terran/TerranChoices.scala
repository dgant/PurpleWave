package Strategery.Strategies.Options.Terran

import Strategery.Strategies.Options.Terran.Global.{ProxyBBS2StartLocations, ProxyBBS3StartLocations}
import Strategery.Strategies._

object TerranChoices {
  
  val overall: Iterable[Strategy] = Vector(
    ProxyBBS2StartLocations,
    ProxyBBS3StartLocations)
}