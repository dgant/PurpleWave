package Strategery.Strategies.Zerg

import Strategery.Strategies.Zerg.Global._
import Strategery.Strategies._

object ZergChoices {
  
  val overall: Iterable[Strategy] = Vector(
    Zerg4PoolAllIn,
    Zerg9Hatch9PoolProxyAllInZerglings,
    Zerg9Hatch9PoolProxyAllInHydras,
    Zerg2HatchMutaAllIn,
    Zerg3HatchHydra)
}