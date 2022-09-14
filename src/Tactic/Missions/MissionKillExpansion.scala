package Tactic.Missions

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Utilities.UnitCounters.CountExactly
import Utilities.UnitFilters.{IsAll, IsAntiGround, IsWarrior}
import Utilities.UnitPreferences.PreferClose
import Tactic.Squads.SquadAutomation
import Utilities.Time.Minutes

class MissionKillExpansion extends Mission {

  def eligible: Seq[Base] = With.geography.bases.filter(b => b.isEnemy && baseFarFromMain(b) && baseCloserToOurArmy(b))

  def best: Option[Base] = Maff.maxBy(eligible)(base =>
    2 * base.heart.groundPixels(With.scouting.enemyThreatOrigin)
      - base.heart.groundPixels(With.scouting.ourMuscleOrigin))

  override def shouldForm: Boolean = (With.blackboard.wantToAttack()
    && With.scouting.ourProximity < 0.5
    && eligible.nonEmpty
    && With.recruiter.available.count(lock.matcher) >= 20)

  var lastFrameInBase = 0
  lock.matcher = IsAll(IsWarrior, IsAntiGround)

  override def recruit(): Unit = {
    val targetBase = best
    if (targetBase.isEmpty) { terminate("No target base available"); return }
    vicinity = targetBase.get.heart.center
    lock.preference = PreferClose(vicinity)
    lock.counter = CountExactly(unitsRequired(targetBase.get))
    lock.acquire()
  }

  private def baseFarFromMain(base: Base): Boolean = With.scouting.enemyMain.forall(b => b.metro != base.metro && ! b.natural.contains(base))
  private def baseCloserToOurArmy(base: Base): Boolean = base.heart.groundPixels(With.scouting.ourMuscleOrigin) + 320 < base.heart.groundPixels(With.scouting.enemyThreatOrigin)
  private def unitsRequired(base: Base): Int = Math.max(4, base.enemies.view.count(u => u.canAttack && ! u.unitClass.isWorker) * 3)
  private def unitsRequired(): Int = vicinity.base.map(unitsRequired).sum
  private def enoughKillers: Boolean = Maff.orElse(units, With.recruiter.available.filter(IsWarrior)).size >= unitsRequired()

  override def run(): Unit = {
    if (vicinity.base.exists(b => units.exists(_.base.contains(b)))) {
      lastFrameInBase = With.frame
    }
    if (duration > Minutes(3)()) { terminate("Exceeded duration"); return }
    if (With.framesSince(Math.max(launchFrame, lastFrameInBase)) > Minutes(1)()) { terminate("Left base or never made it in"); return }
    if ( ! vicinity.base.exists(_.isEnemy)) { terminate("Vicinity not an eligible base"); return }
    if ( ! enoughKillers) { terminate(f"Not enough fighters: ${units.size} vs ${unitsRequired()} "); return }

    SquadAutomation.targetRaid(this)
    SquadAutomation.formAndSend(this)
  }
}
