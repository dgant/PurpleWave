package Tactics.Squads

import Lifecycle.With
import Mathematics.Maff
import Micro.Agency.Intention
import Planning.Predicates.MacroFacts
import Planning.UnitCounters.CountEverything
import Planning.UnitMatchers.{MatchFlyingWarriors, MatchOr}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.Seconds

class SquadAcePilots extends Squad {
  val acePilots = Seq(Terran.Wraith, Terran.Valkyrie, Protoss.Corsair, Zerg.Mutalisk, Zerg.Scourge)
  val acePilotMatcher = MatchOr(acePilots: _*)

  val splashPilots = Seq(Terran.Valkyrie, Protoss.Corsair)
  val splashPilotMatcher = MatchOr(splashPilots: _*)

  lock.matcher = acePilotMatcher
  lock.counter = CountEverything
  override def launch(): Unit = {
    lock.acquire()
  }

  override def run(): Unit = {
    // Help other squads with anti-air
    val otherSquads = With.squads.all.view.filterNot(_ == this)
    val squadsToCover = otherSquads.filter(_.targetQueue.exists(_.exists(MatchFlyingWarriors))).toVector
    if (squadsToCover.nonEmpty) {
      val squad = squadsToCover
        .sortBy(_.isInstanceOf[SquadDefendBase])
        .maxBy(_.targetQueue.get.count(MatchFlyingWarriors))
      vicinity = squad.centroidAll
      SquadAutomation.targetAndSend(this)
      return
    }

    val weSplash = units.exists(splashPilotMatcher)
    val enemySplashes = With.units.enemy.exists(splashPilotMatcher)

    // If they have fast fliers,
    // and we don't have enough to contend against them,
    // seek shelter with the ground army
    val requireFleet = MacroFacts.enemyHasShown(acePilots: _*) || With.units.enemy.exists(u => u.complete && Zerg.Spire(u))
    lazy val hasFleet = units.size >= Math.max(6, if (weSplash && ! enemySplashes) 0 else MacroFacts.enemies(acePilots: _*))
    if (requireFleet && ! hasFleet) {
      chill()
      return
    }

    // Aggressively engage air divisions
    val aceDivision =
      Maff.minBy(With.battles.divisions
        .view
        .filter(_.enemies.exists(MatchFlyingWarriors))
        .filter(d => hasFleet || units.size > d.enemies.count(MatchFlyingWarriors)))(_.centroidAir.pixelDistanceSquared(centroidAir))
    if (aceDivision.isDefined) {
      vicinity = aceDivision.get.centroidAir
      SquadAutomation.targetAndSend(this)
      return
    }

    // Help clear detection
    val squadsFacingDetection = otherSquads.filter(s => s.units.exists(u => Protoss.DarkTemplar(u) && u.matchups.enemyDetectors.nonEmpty)).toVector
    if (squadsFacingDetection.nonEmpty) {
      val squad = squadsFacingDetection.minBy(_.centroidAir.pixelDistanceSquared(centroidAir))
      vicinity = squad.centroidAll
      SquadAutomation.targetAndSend(this)
      return
    }

    // Scout their bases
    // Don't look to target anything; just get scouting information first
    val unscouted = With.geography.enemyBases.filter(b => With.framesSince(b.lastScoutedFrame) > Seconds(30)())
    if (unscouted.nonEmpty) {
      val base = unscouted.minBy(_.townHallArea.center.pixelDistanceSquared(centroidAir))
      vicinity = base.townHallArea.center
      SquadAutomation.send(this)
      return
    }

    // Just snipe anything we can
    val nearestFlier = Maff.minBy(With.units.enemy.filter(u => u.flying && ! u.unitClass.isBuilding))(_.pixelDistanceSquared(centroidAir))
    if (nearestFlier.isDefined) {
      vicinity = nearestFlier.get.pixel
      SquadAutomation.targetAndSend(this)
      return
    }

    chill()
  }

  private def chill(): Unit = {
    val groundToAir = Maff.exemplarOption(With.units.ours.filter(u => u.canAttackAir && ! u.flying).map(_.pixel))
    vicinity = groundToAir.getOrElse(homeConsensus)
    SquadAutomation.target(this)
    units.foreach(_.intend(this, new Intention { toTravel = Some(vicinity); toReturn = Some(vicinity)}))
  }
}
