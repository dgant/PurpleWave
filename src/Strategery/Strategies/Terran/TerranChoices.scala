package Strategery.Strategies.Terran

import Strategery.Strategies.Terran.Global._
import Strategery.Strategies.Terran.Other.TerranFFAMassMarine
import Strategery.Strategies.Terran.TvT.TvTStandard
import Strategery.Strategies.Terran.TvZ.TvZStandard
import Strategery.Strategies._

object TerranChoices {
  
  val overall: Iterable[Strategy] = Vector(
    TvEProxy5RaxAllIn,
    TvEProxyBBS2StartLocations,
    TvEProxyBBS3StartLocations,
    TvEMassMarineAllIn,
    TvTStandard,
    //TvPStandard, // Still doing BBS for now
    TvZStandard,
    TerranFFAMassMarine)
}