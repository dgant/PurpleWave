package Strategery.Strategies

import Strategery.Strategies.AllRaces.WorkerRushes
import Strategery.Strategies.Protoss.ProtossChoices
import Strategery.Strategies.Terran.TerranChoices
import Strategery.Strategies.Zerg.ZergChoices

object AllChoices {
  def treeVsKnownRace : Seq[Strategy] = TerranChoices.all ++ ProtossChoices.all ++ ZergChoices.all ++ WorkerRushes.all
  def treeVsRandom    : Seq[Strategy] = TerranChoices.tvr ++ ProtossChoices.pvr ++ ZergChoices.zvr ++ WorkerRushes.all
  def tree            : Seq[Strategy] = (treeVsKnownRace ++ treeVsRandom).distinct
}
