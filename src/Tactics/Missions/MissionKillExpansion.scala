package Tactics.Missions

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Planning.Predicates.MacroFacts
import Planning.UnitCounters.CountUpTo
import Planning.UnitMatchers.{MatchAnd, MatchAntiGround, MatchWarriors}
import Planning.UnitPreferences.PreferClose
import Tactics.Squads.SquadAutomation
import Utilities.Minutes

class MissionKillExpansion extends Mission {

  def eligible: Seq[Base] = {
    With.geography.enemyBases
      .filterNot(base => With.scouting.enemyMain.exists(_.metro == base.metro))
      .filter(base =>
          base.heart.pixelDistanceGround(With.scouting.ourMuscleOrigin) + 320
        < base.heart.pixelDistanceGround(With.scouting.threatOrigin))
  }

  def best: Option[Base] = Maff.maxBy(eligible)(base =>
    2 * base.heart.pixelDistanceGround(With.scouting.threatOrigin)
      - base.heart.pixelDistanceGround(With.scouting.ourMuscleOrigin))

  override def shouldForm: Boolean = (
    With.blackboard.wantToAttack()
    && MacroFacts.unitsComplete(MatchWarriors) >= 20
    && best.isDefined)

  var lastFrameInBase = 0

  lock.matcher = MatchAnd(MatchWarriors, MatchAntiGround)
  lock.counter = CountUpTo(4)
  override def recruit(): Unit = {
    val targetBase = best
    if (targetBase.isEmpty) {
      terminate("No target base remaining")
      return
    }
    vicinity = targetBase.get.heart.center
    lock.preference = PreferClose(vicinity)
    lock.acquire()
  }

  override def run(): Unit = {
    if (vicinity.base.exists(b => units.exists(_.base.contains(b)))) {
      lastFrameInBase = With.frame
    }
    if (duration > Minutes(3)()) {
      terminate("Exceeded duration")
      return
    }
    if (With.framesSince(lastFrameInBase) > Minutes(1)()) {
      terminate("Left or never made it to the base")
      return
    }
    SquadAutomation.targetFormAndSend(this)
  }
}
