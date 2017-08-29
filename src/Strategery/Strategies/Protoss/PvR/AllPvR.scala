package Strategery.Strategies.Protoss.PvR

import Strategery.Strategies.Protoss.PvP.AllPvP
import Strategery.Strategies.Protoss.PvT.AllPvT
import Strategery.Strategies.Protoss.PvZ.AllPvZ
import Strategery.Strategies.Strategy
import bwapi.Race

object AllPvR extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] =
    AllPvT.choices ++
    AllPvP.choices ++
    AllPvZ.choices ++
    Vector(Vector(
      PvRTinfoil,
      PvREarly2Gate910,
      PvREarly2Gate910AtNatural,
      PvREarly2Gate1012
    ))
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Unknown)
}
