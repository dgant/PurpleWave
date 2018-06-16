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
    OneHatchLurker,
    TwoHatchHydra,
    ZvPHydraRush,
    ZvPNinePool,
    ZvPThirteenPoolMuta,
    ZvPTwoHatchMuta,
    ZvE4Pool,
    ZergSparkle
  )
}