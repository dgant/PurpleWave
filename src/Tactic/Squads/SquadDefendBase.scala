package Tactic.Squads

import Information.Geography.Types.{Base, Edge, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import Micro.Formation.{Formation, FormationEmpty, FormationGeneric, FormationZone}
import Performance.Cache
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.UnitTracking.UnorderedBuffer
import Utilities.Time.Minutes

class SquadDefendBase(base: Base) extends Squad {

  override def launch(): Unit = { /* This squad is given its recruits externally */ }

  lazy val heart: Pixel = if (base.metro == With.geography.ourMain.metro) With.geography.ourMain.heart.center else base.heart.center
  vicinity = heart

  private var lastAction = "Def"
  override def toString: String = f"$lastAction ${base.name.take(5)}"

  val zoneAndChoke = new Cache(() => {
    val zone: Zone = base.zone
    val muscleZone = With.scouting.enemyMuscleOrigin.zone
    var output = (zone, zone.exitNow)
    if ( ! muscleZone.bases.exists(_.owner.isUs)) {
      val possiblePath = With.paths.zonePath(zone, muscleZone)
      possiblePath.foreach(path => {
        val stepScores = path.steps.take(4).filter(_.from.centroid.tileDistanceManhattan(With.geography.home) < 72).indices.map(i => {
          val step          = path.steps(i)
          val turtlePenalty = if (step.to.units.exists(u => u.isOurs && u.unitClass.isBuilding)) 10 else 1
          val altitudeValue = if (With.enemies.forall(_.isZerg)) 1 else 5
          val altitudeDiff  = Maff.signum(step.to.centroid.altitude - step.from.centroid.altitude)
          val altitudeMult  = Math.pow(altitudeValue, altitudeDiff)
          val distanceFrom  = step.edge.pixelCenter.groundPixels(With.geography.home)
          val distanceTo    = step.edge.pixelCenter.groundPixels(With.scouting.threatOrigin)
          val distanceMult  = distanceFrom / Math.max(1.0, distanceFrom + distanceTo)
          val width         = Maff.clamp(step.edge.radiusPixels, 32 * 3, 32 * 16)
          val score         = width * turtlePenalty * altitudeMult * distanceMult // * (3 + i)
          (step, score)
        })
        val scoreBest = Maff.minBy(stepScores)(_._2)
        scoreBest.foreach(s => output = (s._1.from, Some(s._1.edge)))
      })
    }
    output
  })
  private def guardZone: Zone = zoneAndChoke()._1
  private def guardChoke: Option[Edge] = zoneAndChoke()._2
  val bastion = new Cache(() =>
    Maff.minBy(
      base.units.view.filter(u =>
        u.isOurs
        && u.unitClass.isBuilding
        && u.hitPoints < 300
        && (u.friendly.exists(_.knownToEnemy) || u.canAttack)
        && (u.zone != With.geography.ourMain.zone || u.matchups.threats.exists( ! _.unitClass.isWorker))))(u => u.matchups.framesOfSafety + 0.0001 * u.pixelDistanceCenter(heart))
      .map(_.pixel)
      .getOrElse(heart))

  private var formationReturn: Formation = FormationEmpty

  override def run(): Unit = {
    if (units.isEmpty) return

    val canWander         = With.geography.ourBases.size > 2 || ! With.enemies.exists(_.isZerg) || With.blackboard.wantToAttack()
    val scourables        = enemies.filter(isScourable)
    val breached          = scourables.exists(isBreaching)
    val canScour          = scourables.nonEmpty && (canWander || breached)
    val travelGoal        = if (canScour) vicinity else guardChoke.map(_.pixelCenter).getOrElse(bastion())
    val withdrawingUnits  = units.count(u =>
      ! u.metro.contains(base.metro)
      && u.pixelDistanceTravelling(heart) + 32 * 15 > travelGoal.travelPixelsFor(heart, u)
      && u.pixelDistanceTravelling(travelGoal) > 32 * 15)

    lazy val formationWithdraw  = FormationGeneric.disengage(this, Some(travelGoal))
    lazy val formationScour     = FormationGeneric.engage(this, targets.get.headOption.map(_.pixel).getOrElse(vicinity))
    lazy val formationBastion   = FormationGeneric.march(this, bastion())
    lazy val formationGuard     = guardChoke.map(c => FormationZone(this, guardZone, c)).getOrElse(formationBastion)

    val canWithdraw = withdrawingUnits >= Math.max(2, 0.25 * units.size) && formationWithdraw.placements.size > units.size * .75
    val canGuard    = guardChoke.isDefined && (units.size > 5 || ! With.enemies.exists(_.isZerg))

    val targetsUnranked = if (canWithdraw) SquadAutomation.unrankedEnRouteTo(this, vicinity) else if (canScour) scourables else enemies.filter(threateningBase)
    targets = Some(targetsUnranked.sortBy(_.pixelDistanceTravelling(heart)))

    formations.clear()
    if (canWithdraw) {
      lastAction = "Withdraw"
      if (canScour) {
        formations += formationScour
      }
      formations += formationWithdraw
      scour()
    } else if (canScour) {
      lastAction = "Scour"
      formations += formationScour
      formations += formationGuard
      scour()
    } else if (canGuard) {
      lastAction = "Guard"
      formations += formationGuard
      SquadAutomation.send(this)
    } else {
      lastAction = "Hold"
      formations += formationBastion
      SquadAutomation.send(this)
    }
  }

  def intendScouring(scourer: FriendlyUnitInfo, target: UnitInfo): Unit = {
    val squad = this
    scourer.intend(this, new Intention {
      targets = targets.map(target +: _)
      toTravel = Some(target.pixel.traversiblePixel(scourer))
      toReturn = SquadAutomation.getReturn(scourer, squad)
    })
  }

  def scour(): Unit = {
    val assigned = new UnorderedBuffer[FriendlyUnitInfo]()
    val antiAir = new UnorderedBuffer[FriendlyUnitInfo](units.view.filter(_.canAttackAir))
    val antiGround = new UnorderedBuffer[FriendlyUnitInfo](units.view.filter(_.canAttackGround))
    targets.get.foreach(target => {
      val antiTarget = if (target.flying) antiAir else antiGround
      assigned.clear()
      var valueAssigned: Double = 0
      while (antiTarget.nonEmpty && valueAssigned < 3 * target.subjectiveValue) {
        val next = antiTarget.minBy(_.framesToGetInRange(target))
        assigned.add(next)
        antiAir.remove(next)
        antiGround.remove(next)
        valueAssigned += next.subjectiveValue
      }
      val squad = this
      assigned.foreach(intendScouring(_, target))
    })
    if (targets.get.isEmpty) {
      SquadAutomation.send(this)
    } else {
      (antiAir.view ++ antiGround).distinct.foreach(intendScouring(_, targets.get.head))
    }
  }

  private def isBreaching(enemy: UnitInfo): Boolean = (
    enemy.canAttackGround
      && enemy.base.exists(_.owner.isUs)
      && ! enemy.unitClass.isWorker
      && ! guardZone.edges.exists(edge => enemy.pixelDistanceCenter(edge.pixelCenter) < 64 + edge.radiusPixels))

  private def isScourable(enemy: UnitInfo): Boolean = (
    ! (With.frame < Minutes(4)() && Zerg.Drone(enemy) && With.fingerprints.fourPool()) // Don't get baited by 4-pool scouts
    // Don't scour what we can't kill
    && (units.exists(_.canAttack(enemy)) || ((enemy.cloaked || enemy.burrowed) && units.exists(_.unitClass.isDetector)))
    // Don't chase Overlords or floating buildings when there are actual threats nearby
    && ((enemy.unitClass.attacksOrCastsOrDetectsOrTransports && ! Zerg.Overlord(enemy)) || enemies.forall(_.matchups.targets.isEmpty))
    // If we don't really want to fight, wait until they push into the base
    && (enemy.flying || With.blackboard.wantToAttack() || enemy.metro.contains(base.metro)))

  private val enemyHasVision: Cache[Boolean] = new Cache(() => enemies.exists(e => e.flying || e.altitude >= base.heart.altitude))
  private def threateningBase(enemy: UnitInfo): Boolean = {
    if (enemy.zone == base.zone) return true
    // If they're between the bastion and the base
    if ( ! enemy.flying && enemy.pixelDistanceTravelling(base.zone.centroid) < bastion().groundPixels(base.zone.centroid)) return true
    // If they can assault our base from outside it
    if (enemyHasVision() && base.zone.units.view.filter(enemy.inRangeToAttack).exists(u => u.unitClass.melee || ! base.zone.edges.exists(_.contains(u.pixel)))) return true
    false
  }
}
