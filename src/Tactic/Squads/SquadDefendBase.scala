package Tactic.Squads

import Information.Geography.Types.{Base, Edge, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Micro.Actions.Action
import Micro.Agency.{Commander, Intention}
import Micro.Formation._
import Performance.Cache
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.UnitTracking.UnorderedBuffer
import Utilities.Time.Minutes
import Utilities.UnitFilters.IsWorker

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
          val altitudeValue = if (With.enemies.forall(_.isZerg)) 1 else 6
          val altitudeDiff  = Maff.signum(step.to.centroid.altitude - step.from.centroid.altitude)
          val altitudeMult  = Math.pow(altitudeValue, altitudeDiff)
          val distanceFrom  = step.edge.pixelCenter.groundPixels(With.geography.home)
          val distanceTo    = step.edge.pixelCenter.groundPixels(With.scouting.enemyThreatOrigin)
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
  val bastion: Cache[Pixel] = new Cache(() => Maff.minBy(
    base.metro.zones.flatMap(_.units.view.filter(_.isOurs))
      .filter(u => u.unitClass.isBuilding && (u.unitClass.canAttack || (u.complete && u.totalHealth < 300)))
      .map(_.pixel))(_.groundPixels(guardChoke.map(_.pixelCenter).getOrElse(heart))).getOrElse(heart))

  private var formationReturn: Formation = FormationEmpty

  override def run(): Unit = {
    if (units.isEmpty) return
    if (emergencyDTHugs()) return

    val canWander         = With.geography.ourBases.size > 2 || ! With.enemies.exists(_.isZerg) || With.blackboard.wantToAttack()
    val scourables        = enemies.filter(isScourable)
    val breached          = scourables.exists(isBreaching)
    val canScour          = scourables.nonEmpty && (canWander || breached)
    val travelGoal        = if (canScour) vicinity else guardChoke.map(_.pixelCenter).getOrElse(bastion())
    val withdrawingUnits  = units.count(u =>
      ! u.metro.contains(base.metro)
      && u.pixelDistanceTravelling(heart) + 32 * 15 > travelGoal.travelPixelsFor(heart, u)
      && u.pixelDistanceTravelling(travelGoal) > 32 * 15)

    lazy val formationWithdraw  = Formations.disengage(this, Some(travelGoal))
    lazy val formationScour     = Formations.march(this, targets.get.headOption.map(_.pixel).getOrElse(vicinity))
    lazy val formationBastion   = Formations.march(this, bastion())
    lazy val formationGuard     = guardChoke.map(c => new FormationStandard(this, FormationStyleGuard, c.pixelCenter, Some(guardZone))).getOrElse(formationBastion)

    val canWithdraw = withdrawingUnits >= Math.max(2, 0.25 * units.size) && formationWithdraw.placements.size > units.size * .75
    val canGuard    = guardChoke.isDefined && (units.size > 5 || ! With.enemies.exists(_.isZerg))

    val targetsUnranked = if (canScour) scourables else if (canWithdraw) SquadAutomation.unrankedEnRouteTo(this, vicinity) else enemies.filter(threateningBase)
    setTargets(targetsUnranked.sortBy(_.pixelDistanceTravelling(heart)))

    formations.clear()
    if (canWithdraw) {
      lastAction = "Withdraw"
      if (canScour) {
        formations += formationScour
      }
      formations += formationWithdraw
      if (canScour) scour() else SquadAutomation.send(this)
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
    val slot = formations.headOption.flatMap(_(scourer))
    val where = slot
      .filter(p => scourer.pixelsToGetInRangeFrom(target, p) <= scourer.pixelsToGetInRange(target))
      .orElse(slot.map(p => target.pixel.project(p, scourer.pixelRangeAgainst(target))))
      .getOrElse(target.pixel)
      .traversiblePixel(scourer)
    scourer.intend(this)
      .setTargets(target)
      .setTravel(where)
      .setReturnTo(SquadAutomation.getReturn(scourer, squad))
  }

  def scour(): Unit = {
    if (targets.get.isEmpty) {
      SquadAutomation.send(this) // This is an error case; how can we scour if there's nothing to scour?
      return
    }
    val assigned    = new UnorderedBuffer[FriendlyUnitInfo]()
    val antiAir     = new UnorderedBuffer[FriendlyUnitInfo](units.view.filter(_.canAttackAir))
    val antiGround  = new UnorderedBuffer[FriendlyUnitInfo](units.view.filter(_.canAttackGround))
    Seq(3, 6, 9).foreach(multiplier => {
      targets.get.foreach(target => {
        val antiTarget = if (target.flying) antiAir else antiGround
        assigned.clear()
        var valueAssigned: Double = 0
        while (antiTarget.nonEmpty && valueAssigned < multiplier * target.subjectiveValue) {
          // Send all follower units, like Carriers, to the most urgent target, because otherwise they'll just follow the leader
          // SquadAcePilots will often preempt this logic due to substantial overlap between followers/aces
          val antiTargetFollowers = antiTarget.view.filter(_.unitClass.followingAllowed)
          val next = Maff.orElse(antiTargetFollowers, Maff.minBy(antiTarget)(_.framesToGetInRange(target))).toVector // toVector necessary because we're about to modify the underlying collection
          assigned.addAll(next)
          antiAir.removeAll(next)
          antiGround.removeAll(next)
          valueAssigned += next.map(_.subjectiveValue).sum
        }
        assigned.foreach(intendScouring(_, target))
      })
    })
    (antiAir.view ++ antiGround).distinct.foreach(intendScouring(_, targets.get.head))
  }

  private def isBreaching(enemy: UnitInfo): Boolean = (
    enemy.canAttackGround
      && (enemy.base.exists(_.owner.isUs) || guardChoke.exists(c => enemy.metro.exists(_.bases.contains(base)) && enemy.pixel.walkableTile.groundPixels(base.heart) < c.pixelCenter.walkableTile.groundPixels(base.heart)))
      && (With.fingerprints.workerRush() || ! IsWorker(enemy))
      && ! guardZone.edges.exists(edge => enemy.pixelDistanceCenter(edge.pixelCenter) < 64 + edge.radiusPixels))

  private def isScourable(enemy: UnitInfo): Boolean = (
    // Don't get baited by 4-pool scouts
    ! (With.frame < Minutes(4)() && Zerg.Drone(enemy) && With.fingerprints.fourPool())
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

  private def emergencyDTHugs(): Boolean = {
    val dts = enemies.filter(Protoss.DarkTemplar)
    if (dts.nonEmpty && ! With.units.ours.exists(u => u.unitClass.isDetector && u.complete) && enemies.forall(e => Protoss.DarkTemplar(e) || IsWorker(e) || ! e.canAttackGround)) {
      val inOurMain = dts.filter(_.base.contains(With.geography.ourMain))
      val target    = Maff.minBy(inOurMain.map(_.pixel))(_.groundPixels(With.geography.home)).getOrElse(With.geography.ourNatural.zone.exitNowOrHeart.center)
      units.foreach(_.intend(this, new Intention {
        action = new HugAt(target)
      }))
      return true
    }
    false
  }

  private class HugAt(pixel: Pixel) extends Action {
    override def perform(unit: FriendlyUnitInfo): Unit = {
      unit.agent.toTravel = Some(pixel)
      Commander.move(unit)
    }
  }
}
