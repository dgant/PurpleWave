package Tactic.Missions

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.UnitCounters.CountExactly
import Utilities.UnitFilters.{IsAll, IsAntiGround, IsMobileDetector, IsWarrior}
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

  val detectorLock: LockUnits = new LockUnits(this, IsMobileDetector).setCounter(CountExactly(1))

  override def recruit(): Unit = {
    val targetBase = best
    if (targetBase.isEmpty) { terminate("No target base available"); return }
    vicinity = targetBase.get.heart.center
    lock
      .setCounter(CountExactly(unitsRequired(targetBase.get)))
      .setPreference(PreferClose(vicinity))
      .acquire()
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

    if (With.unitsShown.any(Terran.Wraith, Terran.Ghost, Terran.SpiderMine, Terran.Vulture, Protoss.Arbiter, Protoss.DarkTemplar, Zerg.Lurker, Zerg.LurkerEgg) || With.enemies.exists(_.hasTech(Zerg.Burrow))) {
      detectorLock.setPreference(PreferClose(vicinity)).acquire()
    }

    SquadAutomation.targetRaid(this)
    SquadAutomation.formAndSend(this)
    units.foreach(_.intent.setCanSneak(true))
  }
}
