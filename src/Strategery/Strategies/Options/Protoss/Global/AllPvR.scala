package Strategery.Strategies.Options.Protoss.Global

import Strategery.Strategies.Options.Protoss.PvR.{PvREarly2Gate1012, PvREarly2Gate99, PvREarly2Gate99AtNatural}
import Strategery.Strategies.Strategy
import bwapi.Race

object AllPvR extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] =
    AllPvT.choices ++
    AllPvP.choices ++
    AllPvZ.choices ++
    Vector(Vector(
      PvREarly2Gate99,
      PvREarly2Gate99AtNatural,
      PvREarly2Gate1012
    ))
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Unknown)
}
