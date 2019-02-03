package Strategery.Strategies.Zerg

import Strategery.Strategies._

object ZergChoices {
  
  val all: Iterable[Strategy] = Vector(
    ZvESparkle,
    ZvE4Pool,

    ZvTProxyHatchZerglings,
    ZvTProxyHatchHydras,
    ZvTProxyHatchSunkens,
    ZvT1HatchLurker,
    ZvT2HatchLingBustMuta,
    ZvT3HatchLing,
    ZvT2HatchLurker,
    ZvP3Hatch,
    ZvP6Hatch,
    ZvT13PoolMuta,
    ZvP2HatchMuta,
    ZvZ5PoolSunkens,
    ZvZ9PoolSpeed,
    ZvZ12Pool,
    ZvZ10HatchLing
  )
}