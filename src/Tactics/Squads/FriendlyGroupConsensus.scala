package Tactics.Squads

import Mathematics.Maff
import Performance.Cache
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

trait FriendlyGroupConsensus extends GroupConsensus {

  protected def consensusFriendlyUnits: Seq[FriendlyUnitInfo]
  final protected def consensusUnits: Seq[UnitInfo] = consensusFriendlyUnits

  def fightConsensus  = _fightConsensus()
  def originConsensus = _originConsensus()

  private val _fightConsensus   = new Cache(() => Maff.mode(consensusFriendlyUnits.view.map(_.agent.shouldEngage)))
  private val _originConsensus  = new Cache(() => Maff.mode(consensusFriendlyUnits.view.map(_.agent.defaultOrigin)))
}
