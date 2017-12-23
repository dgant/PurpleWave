package Strategery.Strategies.Terran

import Strategery.Strategies.Terran.FFA.TerranFFA
import Strategery.Strategies.Terran.TvE._
import Strategery.Strategies.Terran.TvT.TvTStandard
import Strategery.Strategies.Terran.TvZ.TvZStandard
import Strategery.Strategies._

object TerranChoices {
  
  val all: Iterable[Strategy] = Vector(
    TvEProxy5RaxAllIn,
    TvEProxyBBS2StartLocations,
    TvEProxyBBS3StartLocations,
    TvEMassMarineAllIn,
    TvTStandard,
    TvZStandard,
    TerranFFA)
}