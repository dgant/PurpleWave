package Strategery.Strategies.Options.Terran

import Strategery.Strategies.Options.Terran.Global.{MassMarineAllIn, Proxy5Rax, ProxyBBS2StartLocations, ProxyBBS3StartLocations}
import Strategery.Strategies._

object TerranChoices {
  
  val overall: Iterable[Strategy] = Vector(
    Proxy5Rax,
    ProxyBBS2StartLocations,
    ProxyBBS3StartLocations,
    MassMarineAllIn)
}