package Tactic.Squads

import Information.Battles.Types.CombatGroup
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
  def confidence11          : Double                = { refreshCombatOwnership(); _confidence11() }
  def fightConsensus        : Boolean               = { refreshCombatOwnership(); _fightConsensus() }
  def homeConsensus         : Pixel                 = _homeConsensus()
  def unintended            : Seq[FriendlyUnitInfo] = groupFriendlyOrderable.filter(_.intent.frameCreated < With.frame)

  private var combatRevision = -1L
  private def refreshCombatOwnership(): Unit = synchronized {
    if (combatRevision != CombatGroup.revision) {
      _confidence11.invalidate()
      _fightConsensus.invalidate()
      combatRevision = CombatGroup.revision
    }
  }

  private val _restrainedFrames     = new Cache(() => Maff.mean(groupFriendlyAttackers.filter(_.battle.isDefined).map(_.agent.combat.restrainedFrames)))
  private val _confidence11         = new Cache(() => Maff.mean(Maff.orElse(groupFriendlyOrderable.filter(_.battle.isDefined), groupFriendlyOrderable).map(_.confidence11)))
  private val _fightConsensus       = new Cache(() => Maff.modeOpt(groupFriendlyOrderable.view.map(u => u.combatJudgment.map(j => u.agent.shouldFight || j.unitShouldFight(u)).getOrElse(u.battle.isEmpty && u.agent.shouldFight))).getOrElse(true))
  private val _homeConsensus        = new Cache(() => Maff.modeOpt(groupFriendlyOrderable.view.map(_.agent.defaultHome)).getOrElse(With.geography.home.center))
}
