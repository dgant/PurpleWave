package Strategery.Strategies.Zerg

import Strategery.Strategies.Zerg.ZvE._
import Strategery.Strategies.Zerg.ZvP.HydraBust
import Strategery.Strategies.Zerg.ZvT.TwoHatchMuta
import Strategery.Strategies._

object ZergChoices {
  
  val all: Iterable[Strategy] = Vector(
    Zerg4PoolAllIn,
    ProxyHatchZerglings,
    ProxyHatchHydras,
    ProxyHatchSunkens,
    //ProxySunkens,
    NinePoolMuta,
    NineHatchLings,
    OneHatchLurker,
    HydraBust,
    TwoHatchMuta
  )
}