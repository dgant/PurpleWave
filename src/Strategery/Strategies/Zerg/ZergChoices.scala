package Strategery.Strategies.Zerg

import Strategery.Strategies.Zerg.Global.{Zerg2HatchMutaAllIn, Zerg3HatchHydra, Zerg4PoolAllIn, Zerg9Hatch9PoolProxyAllIn}
import Strategery.Strategies._

object ZergChoices {
  
  val overall: Iterable[Strategy] = Vector(
    Zerg4PoolAllIn,
    Zerg9Hatch9PoolProxyAllIn,
    Zerg2HatchMutaAllIn,
    Zerg3HatchHydra)
}