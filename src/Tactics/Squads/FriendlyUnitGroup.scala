package Tactics.Squads

import Mathematics.Maff
import Performance.Cache
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

import scala.collection.SeqView

trait FriendlyUnitGroup extends UnitGroup {

  def groupFriendlyUnits: Seq[FriendlyUnitInfo]
  def groupUnits: Seq[UnitInfo] = groupFriendlyUnits

  def groupFriendlyOrderable: SeqView[FriendlyUnitInfo, Seq[FriendlyUnitInfo]] = groupFriendlyUnits.view.filter(_.unitClass.orderable)
  def fightConsensus  = _fightConsensus()
  def homeConsensus   = _homeConsensus()

  private val _fightConsensus = new Cache(() => Maff.mode(groupFriendlyOrderable.view.map(_.agent.shouldEngage)))
  private val _homeConsensus  = new Cache(() => Maff.mode(groupFriendlyOrderable.view.map(_.agent.home)))
}