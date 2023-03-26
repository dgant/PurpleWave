package Strategery.Strategies

import Strategery.Strategies.AllRaces.Sandbox
import Strategery.Strategies.Protoss.ProtossChoices
import Strategery.Strategies.Terran.TerranChoices
import Strategery.Strategies.Zerg.ZergChoices

object AllChoices {
  def treeVsKnownRace : Seq[Strategy] = TerranChoices.all ++ ProtossChoices.all ++ ZergChoices.all :+ Sandbox
  def treeVsRandom    : Seq[Strategy] = TerranChoices.tvr ++ ProtossChoices.vsRandom ++ ZergChoices.zvr :+ Sandbox
  def tree            : Seq[Strategy] = (treeVsKnownRace ++ treeVsRandom).distinct
}
