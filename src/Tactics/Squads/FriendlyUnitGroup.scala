package Tactics.Squads

import Mathematics.Maff
import Performance.Cache
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

trait FriendlyUnitGroup extends UnitGroup {

  def groupFriendlyUnits: Seq[FriendlyUnitInfo]
  def groupUnits: Seq[UnitInfo] = groupFriendlyUnits

  def fightConsensus  = _fightConsensus()
  def originConsensus = _originConsensus()

  private val _fightConsensus   = new Cache(() => Maff.mode(groupFriendlyUnits.view.map(_.agent.shouldEngage)))
  private val _originConsensus  = new Cache(() => Maff.mode(groupFriendlyUnits.view.map(_.agent.defaultOrigin)))
}
