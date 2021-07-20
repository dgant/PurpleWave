package Tactics.Missions

import Lifecycle.With
import Planning.Predicates.MacroFacts
import Planning.UnitMatchers.{MatchAnd, MatchComplete}
import ProxyBwapi.Races.Protoss
import Utilities.Seconds

class MissionSpeedlotDrop extends MissionDrop {
  override protected def additionalFormationConditions: Boolean = (
    MacroFacts.upgradeComplete(Protoss.ShuttleSpeed, Seconds(15)())
    && MacroFacts.upgradeComplete(Protoss.ZealotSpeed, Seconds(15)())
    && With.units.countOurs(MatchAnd(Protoss.Zealot, MatchComplete)) >= 4)

  override protected def raid(): Unit = ???

  override protected def recruit(): Unit = ???
}
