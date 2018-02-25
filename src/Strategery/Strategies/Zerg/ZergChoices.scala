package Strategery.Strategies.Zerg

import Strategery.Strategies.Zerg.ZvE._
import Strategery.Strategies.Zerg.ZvP.{HydraBust, TwoHatchHydra}
import Strategery.Strategies.Zerg.ZvT.TwoHatchMuta
import Strategery.Strategies._

object ZergChoices {
  
  val all: Iterable[Strategy] = Vector(
    //ZvPReactive,
    Zerg4PoolAllIn,
    ProxyHatchZerglings,
    ProxyHatchHydras,
    ProxyHatchSunkens,
    NinePoolMuta,
    NineHatchLings,
    OneHatchLurker,
    HydraBust,
    TwoHatchMuta,
    TwoHatchHydra
  )
}