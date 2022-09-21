package Tactic.Squads

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Performance.Cache
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

trait FriendlyUnitGroup extends UnitGroup {

  def groupFriendlyUnits      : Seq[FriendlyUnitInfo]
  def groupFriendlyOrderable  : Seq[FriendlyUnitInfo] = groupFriendlyUnits.view.filter(_.unitClass.orderable)
  def groupUnits              : Seq[UnitInfo]         = groupFriendlyUnits

  def confidence11    : Double  = _confidence11()
  def fightConsensus  : Boolean = _fightConsensus()
  def homeConsensus   : Pixel   = _homeConsensus()

  private val _confidence11     = new Cache(() => Maff.mean(Maff.orElse(groupFriendlyOrderable.filter(_.battle.isDefined), groupFriendlyOrderable).map(_.confidence11)))
  private val _fightConsensus   = new Cache(() => Maff.modeOpt(groupFriendlyOrderable.view.map(u => u.agent.shouldEngage || u.battle.forall(_.judgement.exists(_.unitShouldFight(u))))).getOrElse(true))
  private val _homeConsensus    = new Cache(() => Maff.modeOpt(groupFriendlyOrderable.view.map(_.agent.home)).getOrElse(With.geography.home.center))
}
