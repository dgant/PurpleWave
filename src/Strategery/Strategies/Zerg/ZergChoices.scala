package Strategery.Strategies.Zerg

import Strategery.Strategies._

object ZergChoices {
  
  val all: Iterable[Strategy] = Vector(
    FivePoolProxySunkens,
    ProxyHatchZerglings,
    ProxyHatchHydras,
    ProxyHatchSunkens,
    NineHatchLings,
    NinePoolMuta,
    ThirteenPoolMuta,
    OneHatchLurker,
    TwoHatchHydra,
    ZvPTwoHatchMuta,
    ZvPNinePool,
    ZvE4Pool,
    ZergSparkle
  )
}