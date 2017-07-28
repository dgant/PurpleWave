package Strategery.Strategies.Options

import Strategery.Strategies.Options.Zerg.Global.{Zerg2HatchMutaAllIn, Zerg4PoolAllIn, Zerg9Hatch9PoolProxyAllIn}
import Strategery.Strategies._

object ZergChoices {
  
  val options: Iterable[Strategy] = Vector(
    Zerg4PoolAllIn,
    Zerg9Hatch9PoolProxyAllIn,
    Zerg2HatchMutaAllIn)
}