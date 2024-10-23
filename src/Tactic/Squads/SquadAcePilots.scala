package Tactic.Squads

import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Facts.MacroFacts
import Mathematics.Maff
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import Tactic.Missions.MissionDrop
import Utilities.UnitCounters.CountEverything
import Utilities.UnitFilters.{IsAny, IsFlyingWarrior, UnitFilter}

class SquadAcePilots extends Squad {
  val acePilots         : Seq[UnitClass]  = Seq(Terran.Wraith, Terran.Valkyrie, Protoss.Corsair, Zerg.Mutalisk, Zerg.Scourge)
  val acePilotMatcher   : UnitFilter      = IsAny(acePilots: _*)
  val splashPilots      : Seq[UnitClass]  = Seq(Terran.Valkyrie, Protoss.Corsair)
  val splashPilotMatcher: UnitFilter      = IsAny(splashPilots: _*)

  lock.matcher = acePilotMatcher
  lock.counter = CountEverything
  override def launch(): Unit = {
    if (With.yolo.active) return
    lock.acquire()
  }

  var activity: String = toString
  var hasFleet: Boolean = false
  override def toString: String = activity

  override def run(): Unit = {
    val lastActivity = activity
    chooseActivity()
    if (activity != lastActivity) {
      With.logger.debug(f"Pilots switch from $lastActivity to $activity")
    }
  }

  private def chooseActivity(): Unit = {
    val otherSquads   = With.squads.all.view.filterNot(==)
    val weSplash      = units.exists(splashPilotMatcher)
    val enemySplashes = With.units.enemy.exists(splashPilotMatcher)
    val requireFleet  = MacroFacts.enemyHasShown(acePilots: _*) || MacroFacts.enemiesCompleteFor(Zerg.Scourge.buildFrames, Zerg.Spire) > 0

    // If they have fast fliers,
    // and we don't have enough to contend against them,
    // seek shelter with the ground army
    hasFleet = units.size >= Math.max(6, if (weSplash && ! enemySplashes) 0 else MacroFacts.enemies(acePilots: _*))
    if (requireFleet && ! hasFleet) {
      activity = "AceChill"
      chill()
      return
    }

    // Aggressively engage air divisions
    val aceDivisions = With.battles.divisions
      .view
      .filter(_.enemies.exists(IsFlyingWarrior))
      .filter(d => hasFleet || units.size > d.enemies.count(IsFlyingWarrior))
    val aceDivision = aceDivisions
      .sortBy(d => d.centroidAir.pixelDistanceSquared(centroidAir))
      .sortBy(d => if (weSplash) d.enemies.size else 1)
      .headOption
    if (aceDivision.isDefined) {
      activity = "AceAir2Air"
      vicinity = aceDivision.get.centroidAir
      SquadAutomation.targetAndSend(this)
      return
    }

    // Help clear detection
    val squadsFacingDetection = otherSquads.filter(s => s.units.exists(u => Protoss.DarkTemplar(u) && u.matchups.groupVs.detectors.nonEmpty && u.matchups.groupVs.detectors.forall(_.flying))).toVector
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

    // Help other squads with anti-air
    val squadsToCover = otherSquads.filter(_.targets.exists(_.exists(IsFlyingWarrior))).toVector
    if (squadsToCover.nonEmpty) {
      val squad = squadsToCover
        .sortBy( ! _.isInstanceOf[SquadDefendBase])
        .maxBy(_.targets.get.count(IsFlyingWarrior))
      activity = "AceHelpSquad"
      followSquad(squad)
      return
    }

    // Monitor their bases
    // Don't look to target anything; just get scouting information first
    val unscoutedEnemy = getStaleBase(With.geography.enemyBases, 120)
    if (unscoutedEnemy.nonEmpty) {
      activity = "AceMonitor"
      val base = unscoutedEnemy.minBy(_.enemies.count(_.canAttackAir))
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

    // Explore, and snipe
    val staleBases = Maff.orElse(
      getStaleBase(With.geography.enemyBases,   30),
      getStaleBase(With.geography.neutralBases, 30),
      getStaleBase(With.geography.enemyBases,   10)).toVector
    if (staleBases.nonEmpty) {
      activity = "AceExplore"
      val base = staleBases
        .sortBy(_.townHallArea.center.pixelDistanceSquared(centroidAir))
        .minBy(_.enemies.count(_.canAttackAir))
      vicinity = base.townHallArea.center
      SquadAutomation.targetAndSend(this)
      return
    }

    chill()
  }

  private def getStaleBase(bases: Seq[Base], thresholdSeconds: Int): Seq[Base] = {
    bases.filter(b =>
      With.framesSince(b.lastFrameScoutedByUs) > thresholdSeconds
      && ! b.enemies.exists(_.canAttackAir))
  }

  private def chill(): Unit = {
    activity = "AceChill"
    val groundToAir = Maff.exemplarOpt(With.units.ours.filter(u => u.canAttackAir && ! u.flying).map(_.pixel))
    vicinity = groundToAir.getOrElse(homeConsensus)
    SquadAutomation.target(this)
    units.foreach(_.intend(this).setTerminus(vicinity).setRedoubt(vicinity))
  }

  private def followSquad(otherSquad: Squad): Unit = {
    vicinity = otherSquad.vicinity
    val base = vicinity.base
    val targetsUnsorted = Maff.orElse(
      otherSquad.targets.map(_.filter(_.flying)).getOrElse(Seq.empty),
      base.map(_.enemies.filter(_.flying)).getOrElse(Seq.empty),
      SquadAutomation.unrankedAround(this, vicinity))
     Some(targetsUnsorted.toVector.sortBy(_.pixelDistanceSquared(base.map(_.heart.center).getOrElse(vicinity))))
    SquadAutomation.send(this)
  }
}
