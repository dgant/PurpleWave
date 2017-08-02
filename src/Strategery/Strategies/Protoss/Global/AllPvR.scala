package Strategery.Strategies.Protoss.Global

import Strategery.Strategies.Protoss.PvR.{PvREarly2Gate1012, PvREarly2Gate910, PvREarly2Gate910AtNatural}
import Strategery.Strategies.Strategy
import bwapi.Race

object AllPvR extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] =
    AllPvT.choices ++
    AllPvP.choices ++
    AllPvZ.choices ++
    Vector(Vector(
      PvREarly2Gate910,
      PvREarly2Gate910AtNatural,
      PvREarly2Gate1012
    ))
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Unknown)
}
