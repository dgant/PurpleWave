package Strategery.Strategies.Zerg

import Strategery.Strategies.Zerg.Global._
import Strategery.Strategies._

object ZergChoices {
  
  val all: Iterable[Strategy] = Vector(
    Zerg4PoolAllIn,
    ProxyHatchZerglings,
    ProxyHatchHydras,
    ProxyHatchSunkens,
    ProxySunkens,
    Zerg2HatchMutaAllIn,
    Zerg3HatchHydra)
}