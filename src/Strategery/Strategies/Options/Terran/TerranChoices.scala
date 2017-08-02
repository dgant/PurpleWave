package Strategery.Strategies.Options.Terran

import Strategery.Strategies.Options.Terran.Global._
import Strategery.Strategies._

object TerranChoices {
  
  val overall: Iterable[Strategy] = Vector(
    Proxy5Rax,
    ProxyBBS2StartLocations,
    ProxyBBS3StartLocations,
    MassMarineAllIn,
    DestinyCloudFistAllIn)
}