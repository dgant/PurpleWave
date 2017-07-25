package Strategery.Strategies.Options

import Strategery.Strategies.Options.Zerg.Global.{PvZ4PoolAllIn, PvZ9Hatch9PoolAllIn}
import Strategery.Strategies._

object ZergChoices {
  
  val options: Iterable[Strategy] = Vector(
    PvZ4PoolAllIn,
    PvZ9Hatch9PoolAllIn)
}