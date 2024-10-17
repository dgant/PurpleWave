package Strategery

import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.Players.Players
import Strategery.Selection.StrategySelectionFixed
import bwapi.GameType

trait GameFeatures {
  lazy val heightMain       : Double                = With.self.startTile.altitude
  lazy val heightNatural    : Double                = With.geography.ourNatural.townHallTile.altitude
  lazy val isRamped         : Boolean               = heightMain > heightNatural
  lazy val isFlat           : Boolean               = heightMain == heightNatural
  lazy val isInverted       : Boolean               = heightMain < heightNatural
  lazy val isFixedOpponent  : Boolean               = With.configuration.playbook.policy.isInstanceOf[StrategySelectionFixed]
  lazy val rushDistanceMin  : Int                   = Maff.min(With.geography.rushDistances).getOrElse(rushDistanceMean)
  lazy val rushDistanceMax  : Int                   = Maff.max(With.geography.rushDistances).getOrElse(rushDistanceMean)
  lazy val rushDistanceMean : Int                   = Maff.mean(With.geography.rushDistances.map(_.toDouble)).toInt
  lazy val isIslandMap      : Boolean               = With.geography.mains.forall(base1 => With.geography.mains.forall(base2 => base1 == base2 || With.paths.zonePath(base1.zone, base2.zone).isEmpty))
  lazy val isFfa            : Boolean               = With.enemies.size > 1 && ! Players.all.exists(p => p.isAlly) && With.game.getGameType != GameType.Top_vs_Bottom
  lazy val isMoneyMap       : Boolean               = With.geography.mains.forall(b => b.minerals.length >= 16 && b.mineralsLeft > 1500 * 40)
  lazy val map              : Option[StarCraftMap]  = StarCraftMaps.all.find(_())
}
