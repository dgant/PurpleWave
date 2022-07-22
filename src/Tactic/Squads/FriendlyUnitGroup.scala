package Tactic.Squads

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Performance.Cache
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

import scala.collection.SeqView

trait FriendlyUnitGroup extends UnitGroup {

  def groupFriendlyUnits: Seq[FriendlyUnitInfo]
  def groupUnits: Seq[UnitInfo] = groupFriendlyUnits

  def groupFriendlyOrderable: SeqView[FriendlyUnitInfo, Seq[FriendlyUnitInfo]] = groupFriendlyUnits.view.filter(_.unitClass.orderable)

  def fightConsensus  : Boolean = _fightConsensus()
  def homeConsensus   : Pixel   = _homeConsensus()
  def confidence11    : Double  = _confidence11()

  private val _fightConsensus = new Cache(() => Maff.modeOpt(groupFriendlyOrderable.view.map(u => u.agent.shouldEngage || u.battle.forall(_.judgement.exists(_.unitShouldFight(u))))).getOrElse(true))
  private val _homeConsensus  = new Cache(() => Maff.modeOpt(groupFriendlyOrderable.view.map(_.agent.home)).getOrElse(With.geography.home.center))
  private val _confidence11   = new Cache(() => Maff.mean(Maff.orElse(groupFriendlyOrderable.filter(_.battle.isDefined), groupFriendlyOrderable).map(_.confidence11)))
}
