package Strategery.Strategies.Zerg

import Strategery.Strategies.Strategy

object ZergChoices {

  val zvr = Vector(
    ZvE4Pool,
    ZvR9Pool,
    ZvE9Pool2HatchSpeed
  )

  val zvt = Vector(
    ZvT7Pool,
    ZvT2HatchLingBustMuta,
    ZvT3HatchLing,
    ZvT1HatchHydra,
    ZvT1HatchLurker,
    ZvT2HatchLurker,
    ZvT13PoolMuta,

    ZvT12Hatch13Pool,
    ZvT12Hatch11Pool,
    ZvT9Pool
  )

  val zvp = Vector(
    ZvP2HatchMuta,
    ZvP12Hatch,
    ZvPOverpool,
    ZvP9Pool
  )

  val zvz = Vector(
    ZvZ9PoolSpeed,
    ZvZ12Pool,
    ZvZ10HatchLing
  )
  
  val all: Vector[Strategy] = (zvr ++ zvt ++ zvp ++ zvz).distinct
}