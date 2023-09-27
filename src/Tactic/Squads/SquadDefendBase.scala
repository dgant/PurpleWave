package Tactic.Squads

import Debugging.Visualizations.Forces
import Information.Geography.Types.{Base, Edge, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Micro.Actions.{Action, Idle}
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import Micro.Formation._
import Micro.Heuristics.Potential
import Performance.Cache
import Planning.MacroFacts
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.UnitTracking.UnorderedBuffer
import Utilities.?
import Utilities.Time.Minutes
import Utilities.UnitCounters.CountEverything
import Utilities.UnitFilters.{IsDetector, IsTank, IsWarrior, IsWorker}

class SquadDefendBase(base: Base) extends Squad {

  override def launch(): Unit = { /* This squad is given its recruits externally */ }

  private var lastAction = "Def"
  override def toString: String = f"$lastAction ${base.name.take(5)}"

  val workerLock = new LockUnits(this, (u: UnitInfo) => IsWorker(u) && u.base.filter(_.isOurs).forall(base==), CountEverything)

  val plugEdge = new Cache(() =>
    base.metro.bases
      .find(_.isMain)
      .filter(unused => ! With.enemies.exists(_.isZerg) && units.count(u => With.framesSince(u.frameDiscovered) > 240) <= 1)
      .filter(_.zone.exitNow.isDefined)
      .map(main => (main.zone, main.zone.exitNow)))

  val zoneAndChoke: Cache[(Zone, Option[Edge])] = new Cache(() => {
    if (plugEdge().isDefined) {
      plugEdge().get
    } else {
      val zone        = base.zone
      val muscleZone  = With.scouting.enemyMuscleOrigin.zone
      var output      = (zone, zone.exitNow)
      if ( ! muscleZone.bases.exists(_.owner.isUs)) {
        val possiblePath = With.paths.zonePath(zone, muscleZone)
        possiblePath.foreach(path => {
          val stepScores = path.steps.take(4).filter(_.from.centroid.tileDistanceManhattan(With.geography.home) < 72).indices.map(i => {
            val step          = path.steps(i)
            val turtlePenalty = if (step.to.units.exists(u => u.isOurs && u.unitClass.isBuilding)) 10 else 1
            val altitudeValue = if (With.enemies.forall(_.isZerg)) 1 else 6
            val altitudeDiff  = Maff.signum101(step.to.centroid.altitude - step.from.centroid.altitude)
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
    }
  })
  private def guardZone: Zone = zoneAndChoke()._1
  private def guardChoke: Option[Edge] = zoneAndChoke()._2
  val bastion: Cache[Pixel] = new Cache(() =>
      Maff.minBy(
        base.metro.zones
          .flatMap(_.units.view.filter(_.isOurs))
          .filter(u => u.unitClass.isBuilding && (u.unitClass.canAttack || Protoss.ShieldBattery(u) || (u.complete && u.totalHealth < 300)))
          .map(_.pixel))
      (_.groundPixels(
        guardChoke
          .map(_.pixelCenter)
          .getOrElse(vicinity)))
      .getOrElse(vicinity))

  private var formationReturn: Formation = FormationEmpty

  override def run(): Unit = {
    vicinity = ?(base.metro == With.geography.ourMain.metro, With.geography.ourMain, base).heart.center
    if (units.isEmpty) return
    if (emergencyDTHugs()) return

    val canWander         = With.geography.ourBases.size > 2 || ! With.enemies.exists(_.isZerg) || With.blackboard.wantToAttack()
    val scourables        = enemies.filter(isScourable)
    val breached          = scourables.exists(isBreaching)
    val canScour          = scourables.nonEmpty && (canWander || breached)
    val travelGoal        = ?(canScour, vicinity, guardChoke.map(_.pixelCenter).getOrElse(bastion()))
    val withdrawingUnits  = units.count(u =>
      ! u.metro.contains(base.metro)
      && u.pixelDistanceTravelling(vicinity) + 32 * 15 > travelGoal.travelPixelsFor(vicinity, u)
      && u.pixelDistanceTravelling(travelGoal) > 32 * 15)

    val pullWorkers = (
      With.frame < Minutes(8)()
      && withdrawingUnits == 0
      && confidence11 < -0.2
      && breached
      && enemies.exists(_.isAny(Terran.Marine, IsTank, Protoss.Dragoon, Zerg.Zergling, Zerg.Hydralisk))
      && ! enemies.exists(_.isAny(Terran.Firebat, Terran.Vulture, Protoss.DarkTemplar, Protoss.Archon, Protoss.Reaver, Zerg.Lurker, Zerg.Ultralisk)))
    if (pullWorkers) {
      addUnits(workerLock.acquire())
    }

    lazy val formationWithdraw  = Formations.disengage(this, Some(travelGoal))
    lazy val formationScour     = Formations.march(this, targets.get.headOption.map(_.pixel).getOrElse(vicinity))
    lazy val formationGuard     = guardChoke.map(c => new FormationStandard(this, FormationStyleGuard, c.pixelCenter, Some(guardZone))).getOrElse(formationBastion)
    lazy val formationBastion   = {
      val enemyTopSpeed         = Maff.max(enemies.view.filter(u => u.canAttack && ! IsWorker(u)).map(_.topSpeed)).getOrElse(2 * Terran.Vulture.topSpeed)
      val formationBastion      = Formations.march(this, bastion())
      def sendToCenter(unit: FriendlyUnitInfo): Boolean = unit.unitClass.ranged && unit.topSpeed >= enemyTopSpeed
      if (units.exists(sendToCenter)) {
        lazy val formationCenter  = Formations.march(this, bastion().zone.centroid.walkableTile.center)
        new Formation {
          override def style: FormationStyle = formationBastion.style
          override def placements: Map[FriendlyUnitInfo, Pixel] = formationBastion.placements.filterNot(p => sendToCenter(p._1)) ++ formationCenter.placements.filter(p => sendToCenter(p._1))
        }
      } else formationBastion
    }


    val canWithdraw     = withdrawingUnits >= Math.max(2, 0.25 * units.size) && formationWithdraw.placements.size > units.size * .75
    val canGuard        = guardChoke.isDefined && (units.size > 5 || ! With.enemies.exists(_.isZerg) || With.geography.ourBases.length > 1)
    val targetsUnranked =
      if        (canScour)    scourables
      else if   (canWithdraw) SquadAutomation.unrankedEnRouteTo(this, vicinity).toVector
      else                    enemies.filter(threateningBase)
    setTargets(targetsUnranked.sortBy(_.pixelDistanceTravelling(vicinity)).sortBy(IsWorker))

    if (plugEdge().isDefined) {
      val plugPixel = plugEdge().get._2.get.pixelCenter
      lastAction = "Plug"
      formations += FormationSimple(FormationStylePlug, units.map((_, plugPixel)).toMap)
      units.foreach(_.intend(this).setAction(new Plug(plugPixel)))
    } else if (canWithdraw) {
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
      if (targets.exists(_.nonEmpty)) {
        formations += formationScour
      }
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
      .orElse(slot.map(p => target.pixel.project(p, scourer.pixelsToGetInRange(target))))
      .getOrElse(target.pixel)
      .traversiblePixel(scourer)
    scourer.intend(this)
      .setTargets(target)
      .setTerminus(where)
      .setRedoubt(SquadAutomation.getReturn(scourer, squad))
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
        val antiTarget = ?(target.flying, antiAir, antiGround)
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
      && (enemy.base.exists(b => b == base || b.owner.isUs)
        || With.geography.ourBases.exists(_.townHall.exists(th => enemy.inRangeToAttack(th) && th.injury > 0.5))
        || guardChoke.exists(c => enemy.metro.exists(_.bases.contains(base)) && enemy.pixel.walkableTile.groundPixels(base.heart) < c.pixelCenter.walkableTile.groundPixels(base.heart)))
      && (With.fingerprints.workerRush() || ! IsWorker(enemy))
      && ! guardZone.edges.exists(edge => enemy.pixelDistanceCenter(edge.pixelCenter) < 64 + edge.radiusPixels))

  private val enemyCanAttackGround = new Cache(() => enemies.exists(_.canAttackGround))
  private def isScourable(enemy: UnitInfo): Boolean = (
    // Don't get baited by 4-pool scouts
    ! (With.frame < Minutes(4)() && Zerg.Drone(enemy) && With.fingerprints.fourPool())
    // Don't scour what we can't kill
    && (units.exists(_.canAttack(enemy)) || ((enemy.cloaked || enemy.burrowed) && units.exists(_.unitClass.isDetector)))
    // Don't chase Overlords or floating buildings when there are actual threats nearby
    && ((enemy.unitClass.attacksOrCastsOrDetectsOrTransports && ! Zerg.Overlord(enemy)) || ! enemyCanAttackGround())
    // If we don't really want to fight, wait until they push into the base
    && (enemy.flying || With.blackboard.wantToAttack() || enemy.metro.contains(base.metro)))

  private val enemyHasVision: Cache[Boolean] = new Cache(() => enemies.exists(e => e.flying || e.altitude >= base.heart.altitude || e.orderTarget.filter(_.isFriendly).exists(_.base.contains(base))))

  private def threateningBase(enemy: UnitInfo): Boolean = {
    var output = enemy.zone == base.zone
    // If they're between the bastion and the base
    // COG2023: Disabling this check due to suspicion it procs prematurely in small bases
    //output      ||= ! enemy.flying && enemy.pixelDistanceTravelling(base.zone.centroid) < bastion().groundPixels(base.zone.centroid)
    // If they can assault our base from outside it
    output ||= enemyHasVision() && enemies.exists(e =>
      e.presumptiveTarget.flatMap(_.friendly).exists(t =>
        e.pixelsToGetInRange(t, ?(
          t.squad.contains(this),
          t.pixel,
          t.agent.safety)) < 32
        && (t.base.contains(base) || t.squad.contains(this))
        && (e.inRangeToAttack(t) || e.speedApproaching(t) > 0)))
    output
  }

  private def emergencyDTHugs(): Boolean = {
    lazy val enemyNearest   = Maff.minBy(enemies.view.filter(IsWarrior))(_.pixelDistanceTravelling(base.heart))
    lazy val dts            = enemies.filter(Protoss.DarkTemplar)
    lazy val dtApproaching  = dts.exists(_.base.exists(_.isOurs)) || base.zone.exitNow.exists(e => dts.exists(_.pixelDistanceTravelling(e.pixelCenter) < 160))

    var shouldHug   = With.enemies.exists(_.isProtoss)
    shouldHug     &&= With.units.existsEnemy(Protoss.DarkTemplar)
    shouldHug     &&= enemies.exists(Protoss.DarkTemplar)
    shouldHug     &&= ! MacroFacts.haveComplete(IsDetector)
    shouldHug     &&= enemyNearest.forall(Protoss.DarkTemplar) || dtApproaching

    if (shouldHug) {
      val inOurMain = dts.filter(_.base.contains(With.geography.ourMain))
      val target    = Maff.minBy(inOurMain.map(_.pixel))(_.groundPixels(With.geography.home)).getOrElse(With.geography.ourMain.zone.exitNowOrHeart.center)
      val pixels    = With.units.ours.filter(u => u.unitClass.isDetector && ! u.flying).map(_.pixel).toSeq ++ Seq(target)
      val pixel     = pixels.minBy(p => dts.map(_.pixelDistanceTravelling(p)).min)
      units.foreach(_.intend(this).setAction(new HugAt(pixel)))
    }

    shouldHug
  }

  private case class HugAt(pixel: Pixel) extends Action {
    override def perform(unit: FriendlyUnitInfo): Unit = {
      unit.agent.decision.set(pixel)
      Potshot.delegate(unit)
      Commander.move(unit)
    }
  }

  private case class Plug(pixel: Pixel) extends Action {
    override def perform(unit: FriendlyUnitInfo): Unit = {
      if (unit.matchups.threats.exists(IsWarrior) || (unit.unitClass.ranged && unit.matchups.targets.exists(_.base.exists(_.isMain)))) {
        Idle.perform(unit)
      } else {
        var to = pixel
        Potshot.delegate(unit)
        unit.agent.forces(Forces.pushing) = Potential.followPushes(unit)
        if (unit.agent.forces.sum.lengthSquared > 0) {
          MicroPathing.moveForcefully(unit)
        } else {
          if (unit.pixel == pixel) {
            Commander.hold(unit)
            return
          } else if (unit.seeminglyStuck) {
            to = plugEdge().get._1.centroid.center.walkablePixel
          }
          unit.agent.redoubt.set(to)
          unit.agent.decision.set(to)
          Commander.move(unit)
        }
      }
    }
  }
}
