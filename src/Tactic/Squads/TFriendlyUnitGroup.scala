package Tactic.Squads

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Performance.Cache
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

trait TFriendlyUnitGroup {
  def groupFriendlyUnits      : Seq[FriendlyUnitInfo]
  def groupFriendlyOrderable  : Seq[FriendlyUnitInfo] = groupFriendlyUnits.view.filter(_.unitClass.orderable)
  def groupFriendlyAttackers  : Seq[FriendlyUnitInfo] = groupFriendlyOrderable.view.filter(u => u.unitClass.canAttack && ! u.unitClass.isWorker)

  def restrainedFrames      : Double                = _restrainedFrames()
  def confidence11          : Double                = _confidence11()
  def fightConsensus        : Boolean               = _fightConsensus()
  def homeConsensus         : Pixel                 = _homeConsensus()
  def unintended            : Seq[FriendlyUnitInfo] = groupFriendlyOrderable.filter(_.intent.frameCreated < With.frame)

  private val _restrainedFrames     = new Cache(() => Maff.mean(groupFriendlyAttackers.filter(_.battle.isDefined).map(_.agent.combat.restrainedFrames)))
  private val _confidence11         = new Cache(() => Maff.mean(Maff.orElse(groupFriendlyOrderable.filter(_.battle.isDefined), groupFriendlyOrderable).map(_.confidence11)))
  private val _fightConsensus       = new Cache(() => Maff.modeOpt(groupFriendlyOrderable.view.map(u => u.agent.shouldFight || u.battle.forall(_.judgement.exists(_.unitShouldFight(u))))).getOrElse(true))
  private val _homeConsensus        = new Cache(() => Maff.modeOpt(groupFriendlyOrderable.view.map(_.agent.home)).getOrElse(With.geography.home.center))
}
