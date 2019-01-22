package Strategery.Strategies.Zerg

import Strategery.Strategies._

object ZergChoices {
  
  val all: Iterable[Strategy] = Vector(
    ZvZ5PoolSunkenRush,
    ZvTProxyHatchZerglings,
    ZvTProxyHatchHydras,
    ZvTProxyHatchSunkens,
    ZvZ10HatchLing,
    ZvZ9PoolMuta,
    ZvT1HatchLurker,
    ZvPOverpool,
    ZvP3HatchAggro,
    ZvT13PoolMuta,
    ZvP2HatchMuta,
    ZvE4Pool,
    ZvESparkle
  )
}