package Strategery.Strategies.Zerg

import Strategery.Strategies._

object ZergChoices {
  
  val all: Iterable[Strategy] = Vector(
    Zerg4Pool,
    FivePoolProxySunkens,
    ProxyHatchZerglings,
    ProxyHatchHydras,
    ProxyHatchSunkens,
    NineHatchLings,
    NinePoolMuta,
    ThirteenPoolMuta,
    OneHatchLurker,
    HydraBust,
    TwoHatchMuta,
    TwoHatchHydra,
    ZergVsProtoss,
    ZergSparkle
  )
}