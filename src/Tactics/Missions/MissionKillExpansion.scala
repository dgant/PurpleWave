package Tactics.Missions

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Planning.UnitCounters.CountUpTo
import Planning.UnitMatchers.{MatchAnd, MatchAntiGround, MatchWarriors}
import Planning.UnitPreferences.PreferClose
import Tactics.Squads.SquadAutomation
import Utilities.Minutes

class MissionKillExpansion extends Mission {

  def eligible: Seq[Base] = {
    With.scouting.enemyMain.map(main =>
      With.geography.enemyBases
        .filterNot(_.metro == main.metro)
        .filter(b => With.scouting.muscleOrigin.groundPixels(b.heart) + 320 < With.scouting.threatOrigin.groundPixels(b.heart))).getOrElse(Seq.empty)
  }

  def best: Option[Base] = Maff.maxBy(eligible)(b => b.heart.groundPixels(With.scouting.threatOrigin) - b.heart.groundPixels(With.geography.home))

  override def shouldForm: Boolean = With.blackboard.wantToAttack() && best.isDefined

  override def shouldTerminate: Boolean = duration > Minutes(3)() || With.framesSince(lastFrameInBase) > Minutes(1)()

  var lastFrameInBase = 0

  // TODO: Should squads have a built-in default lock? Seems like recipe for bugs
  lock.matcher = MatchAnd(MatchWarriors, MatchAntiGround)
  lock.counter = CountUpTo(4)
  override protected def recruit(): Unit = {
    val targetBase = best
    if (targetBase.isEmpty) {
      terminate()
      return
    }
    vicinity = targetBase.get.heart.center
    lock.preference = PreferClose(vicinity)
    addUnits(lock.acquire(this))
  }

  override def run(): Unit = {
    if (vicinity.base.exists(b => units.exists(_.base.contains(b)))) {
      lastFrameInBase = With.frame
    }
    SquadAutomation.targetAndSend(this)
  }
}
