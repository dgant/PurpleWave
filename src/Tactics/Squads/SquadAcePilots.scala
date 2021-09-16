package Tactics.Squads

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Micro.Agency.Intention
import Planning.Predicates.MacroFacts
import Planning.UnitCounters.CountEverything
import Planning.UnitMatchers.{MatchFlyingWarriors, MatchOr}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Tactics.Missions.MissionDrop
import Utilities.Time.Seconds

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

  var activity: String = toString
  override def toString = activity

  override def run(): Unit = {
    // Help other squads with anti-air
    val otherSquads = With.squads.all.view.filterNot(_ == this)
    val squadsToCover = otherSquads.filter(_.targets.exists(_.exists(MatchFlyingWarriors))).toVector
    if (squadsToCover.nonEmpty) {
      val squad = squadsToCover
        .sortBy(_.isInstanceOf[SquadDefendBase])
        .maxBy(_.targets.get.count(MatchFlyingWarriors))
      activity = "AceHelpSquad"
      followSquad(squad)
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
      activity = "AceChill"
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
      activity = "AceAir2Air"
      vicinity = aceDivision.get.centroidAir
      SquadAutomation.targetAndSend(this)
      return
    }

    // Help clear detection
    val squadsFacingDetection = otherSquads.filter(s => s.units.exists(u => Protoss.DarkTemplar(u) && u.matchups.enemyDetectors.forall(_.flying))).toVector
    if (squadsFacingDetection.nonEmpty) {
      activity = "AceCloakDT"
      followSquad(squadsFacingDetection.minBy(_.centroidAir.pixelDistanceSquared(centroidAir)))
      return
    }

    // Help drop missions
    val dropMissions = otherSquads.filter(_.isInstanceOf[MissionDrop]).map(_.asInstanceOf[MissionDrop])
    if (dropMissions.nonEmpty) {
      activity = "AceEscortDrop"
      followSquad(dropMissions.maxBy(_.duration))
    }

    // Monitor their bases
    // Don't look to target anything; just get scouting information first
    val unscoutedEnemy = getStaleBase(With.geography.enemyBases)
    if (unscoutedEnemy.nonEmpty) {
      activity = "AceMonitor"
      val base = unscoutedEnemy.minBy(_.units.count(u => u.isEnemy && u.canAttackAir))
      vicinity = base.townHallArea.center
      SquadAutomation.send(this)
      return
    }

    // Just snipe anything we can
    val nearestFlier = Maff.minBy(With.units.enemy.filter(u => u.likelyStillThere && u.flying && ! u.unitClass.isBuilding))(_.pixelDistanceSquared(centroidAir))
    if (nearestFlier.isDefined) {
      activity = "AceHunt"
      vicinity = nearestFlier.get.pixel
      SquadAutomation.targetAndSend(this)
      return
    }

    // Explore
    val unscoutedNeutral = getStaleBase(With.geography.enemyBases)
    if (unscoutedNeutral.nonEmpty) {
      activity = "AceExplore"
      val base = unscoutedNeutral
        .sortBy(_.townHallArea.center.pixelDistanceSquared(centroidAir))
        .minBy(_.units.count(u => u.isEnemy && u.canAttackAir))
      vicinity = base.townHallArea.center
      SquadAutomation.send(this)
      return
    }

    chill()
  }

  private def getStaleBase(bases: Seq[Base]): Seq[Base] = {
    bases.filter(b => With.framesSince(b.lastScoutedFrame) > Seconds(30)() && ! b.units.exists(u => u.isEnemy && u.canAttackAir))
  }

  private def chill(): Unit = {
    activity = "AceChill"
    val groundToAir = Maff.exemplarOption(With.units.ours.filter(u => u.canAttackAir && ! u.flying).map(_.pixel))
    vicinity = groundToAir.getOrElse(homeConsensus)
    SquadAutomation.target(this)
    units.foreach(_.intend(this, new Intention { toTravel = Some(vicinity); toReturn = Some(vicinity)}))
  }

  private def followSquad(otherSquad: Squad): Unit = {
    vicinity = otherSquad.centroidAir
    targets = otherSquad.targets
      .map(_.filter(_.flying))
      .orElse(Some(SquadAutomation.rankForArmy(this,
        Maff.orElse(
          SquadAutomation.unrankedEnRouteTo(this, vicinity),
          SquadAutomation.unrankedAround(this, vicinity)).toVector)))
    SquadAutomation.send(this)
  }
}
