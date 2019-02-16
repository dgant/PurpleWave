package Strategery.Strategies.Zerg

import Strategery.Strategies.Strategy

object ZergChoices {

  val zvr = Vector(
    ZvESparkle,
    ZvE4Pool,
    ZvR9Pool
  )

  val zvt = Vector(
    ZvTProxyHatchZerglings,
    ZvTProxyHatchHydras,
    ZvTProxyHatchSunkens,
    ZvT7Pool,
    ZvT2HatchLingBustMuta,
    ZvT3HatchLing,
    ZvT1HatchHydra,
    ZvT1HatchLurker,
    ZvT2HatchLurker,
    ZvT13PoolMuta,
  )

  val zvp = Vector(
    ZvP3Hatch,
    ZvP6Hatch,
    ZvP2HatchMuta,
  )

  val zvz = Vector(
    ZvZ5PoolSunkens,
    ZvZ9PoolSpeed,
    ZvZ12Pool,
    ZvZ10HatchLing
  )
  
  val all: Vector[Strategy] = (zvr ++ zvt ++ zvp ++ zvz).distinct
}