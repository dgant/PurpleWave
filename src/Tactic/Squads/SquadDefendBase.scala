package Tactic.Squads

import Debugging.Visualizations.Forces
import Information.Geography.Types.{Base, Edge, Zone}
import Lifecycle.With
import Macro.Facts.MacroFacts
import Mathematics.Maff
import Mathematics.Points.Pixel
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.{Action, Idle}
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import Micro.Formation._
import Micro.Heuristics.Potential
import Performance.Cache
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.UnitTracking.UnorderedBuffer
import Utilities.?
import Utilities.Time.Minutes
import Utilities.UnitCounters.CountUpTo
import Utilities.UnitFilters._

class SquadDefendBase(base: Base) extends Squad {

  override def launch(): Unit = { /* This squad is given its recruits externally */ }

  def recruitWorkers(invaders: Seq[UnitInfo]): Unit = {
    if (With.frame >= Minutes(8)()) {
      workerLock.release()
      return
    }
    // Recruitment precedes prepareToRun; inspect this cycle's incoming assignments.
    val defenders       = unitsNext.filterNot(IsWorker)
    val (zone, choke)   = zoneAndChokeFor(defenders)
    val defense         = new DefenseState(defenders, invaders, zone, choke)
    lazy val wallMinimum = wallGapsFor.minimumDefenders
    val workersRequired =
      if (defense.breached
        && defense.withdrawingUnits == 0
        && (defenders.isEmpty || GenericFriendlyUnitGroup(defenders).confidence11 < -0.2)
        && invaders.exists(_.isAny(Terran.Marine, IsTank, Protoss.Dragoon, Zerg.Zergling, Zerg.Hydralisk))
        && ! invaders.exists(_.isAny(Terran.Firebat, Terran.Vulture, Protoss.DarkTemplar, Protoss.Archon, Protoss.Reaver, Zerg.Lurker, Zerg.Ultralisk))) {
        invaders.map(e => ?(e.flying || ! e.canAttackGround, 0, ?(IsWorker(e), 1, ?(e.isAny(Protoss.Zealot, Protoss.Dragoon, IsTank, Terran.Vulture, Terran.Goliath), 4, ?(e.isAny(Zerg.Hydralisk, Terran.Goliath), 3, 2))))).sum
      } else if (wallMinimum.isDefined) {
        val gapsOpen    = Math.max(0, wallMinimum.get - defenders.count(Protoss.Zealot))
        val engaged     = invaders.filter(_.matchups.threatsInRange.exists(Protoss.PhotonCannon))
        val approaching = invaders.filter(e => Maff.min(e.matchups.targets.filter(Protoss.PhotonCannon).map(e.pixelDistanceEdge)).exists(_ < 320))
        Math.max(Math.max(
          gapsOpen,
          2 * engaged.length),
          approaching.map(u => Maff.fromBoolean(Zerg.Zergling(u)) + 2 * Maff.fromBoolean(Zerg.Hydralisk(u))).sum
          - 2 * defenders.length
          - 3 * MacroFacts.unitsComplete(Protoss.PhotonCannon))
      } else 0
    if (workersRequired > 0) {
      addUnits(workerLock.setCounter(CountUpTo(workersRequired)).acquire())
    } else {
      workerLock.release()
    }
  }

  private var lastAction = "Def"
  override def toString: String = f"$lastAction ${base.name.take(5)}"

  val workerLock = new LockUnits(this, (u: UnitInfo) => IsWorker(u) && u.metro.contains(base.metro))

  val wallToPlug = new Cache(() => With.placement.wall
    .filter(_ => base.metro == With.geography.ourFoyer.metro)
    .filter(wall => With.units.ours.filter(IsBuilding).map(_.tileTopLeft).exists(t => wall.buildings.exists(_._1 == t)))
    .map(_.defensePoint))

  val edgeToPlug = new Cache(() => edgeToPlugFor(units))
  private def edgeToPlugFor(defenders: Seq[FriendlyUnitInfo]): Option[(Zone, Option[Edge])] =
    base.metro.bases
      .find(_.isMain)
      .filter(unused => ! With.enemies.exists(_.isZerg) && defenders.count(u => With.framesSince(u.frameDiscovered) > 240) <= 1)
      .filter(_.zone.exitNow.isDefined)
      .map(main => (main.zone, main.zone.exitNow))

  val zoneAndChokeToGuard: Cache[(Zone, Option[Edge])] = new Cache(() => zoneAndChokeFor(units))
  private def zoneAndChokeFor(defenders: Seq[FriendlyUnitInfo]): (Zone, Option[Edge]) = {
    lazy val edge = edgeToPlugFor(defenders)
    if (wallToPlug().isDefined) {
      val wallZone = With.geography.ourFoyer.zone
      (wallZone, wallZone.exitOriginal)
    } else if (edge.isDefined) {
      edge.get
    } else {
      val zone        = base.zone
      val muscleZone  = With.scouting.enemyMuscleOrigin.zone
      var output      = (zone, zone.exitNow)
      if ( ! muscleZone.bases.exists(_.owner.isUs)) {
        val possiblePath = With.paths.zonePath(zone, muscleZone)
        possiblePath.foreach(path => {
          val stepScores = path.steps.take(4).filter(_.from.centroid.tileDistanceManhattan(With.geography.home) < 72)
            .map(step => {
            val turtlePenalty = ?(step.to.units.exists(u => u.isOurs && u.unitClass.isBuilding), 10, 1)
            val altitudeValue = ?(With.enemies.forall(_.isZerg), 1, 6)
            val altitudeDiff  = Maff.signum101(step.to.centroid.altitude - step.from.centroid.altitude)
            val altitudeMult  = Math.pow(altitudeValue, altitudeDiff)
            val distanceFrom  = step.edge.pixelCenter.groundPixels(With.geography.home)
            val distanceTo    = step.edge.pixelCenter.groundPixels(With.scouting.enemyThreatOrigin)
            val distanceMult  = distanceFrom / Math.max(1.0, distanceFrom + distanceTo)
            val width         = Maff.clamp(step.edge.radiusPixels, 32 * 3, 32 * 16)
            val score         = width * turtlePenalty * altitudeMult * distanceMult
            (step, score)
          })
          Maff.minBy(stepScores)(_._2).foreach(s => output = (s._1.from, Some(s._1.edge)))
        })
      }
      output
    }
  }
  private def guardZone: Zone = zoneAndChokeToGuard()._1
  private def guardChoke: Option[Edge] = zoneAndChokeToGuard()._2
  private def defensePoint: Pixel = wallToPlug().map(_.center).getOrElse(?(base.metro == With.geography.ourMain.metro, With.geography.ourMain, base).heart.center)

  val bastion: Cache[Pixel] = new Cache(() => bastionFor(guardChoke))
  private def bastionFor(choke: Option[Edge]): Pixel = {
    Maff.minBy(
      base.metro.zones
        .flatMap(_.units.view.filter(_.isOurs))
        .filter(u => u.unitClass.isBuilding && (u.unitClass.canAttack || Protoss.ShieldBattery(u) || (u.complete && u.totalHealth < 300)))
        .map(_.pixel))(_.groundPixels(choke.map(_.pixelCenter).getOrElse(defensePoint)))
    .getOrElse(defensePoint)
  }

  private class DefenseState(defenders: Seq[FriendlyUnitInfo], invaders: Seq[UnitInfo], zone: Zone, choke: Option[Edge]) {
    private lazy val breaching            = invaders.filter(isBreaching(_, zone, choke))
    private lazy val enemyCanAttackGround = invaders.exists(_.canAttackGround)
    // Recruitment must detect invasions even before defenders can scour them.
    lazy val breached         = breaching.nonEmpty
    lazy val scourables       = invaders.filter(isScourable(_, defenders, enemyCanAttackGround))
    lazy val canScour         = scourables.nonEmpty && (
      With.geography.ourBases.size > 2 || ! With.enemies.exists(_.isZerg) || With.blackboard.wantToAttack() || scourables.exists(breaching.contains))
    lazy val travelGoal       = ?(canScour, defensePoint, choke.map(_.pixelCenter).getOrElse(bastionFor(choke)))
    lazy val withdrawingUnits = defenders.count(u =>
      ! u.metro.contains(base.metro)
      && u.pixelDistanceTravelling(defensePoint) + 32 * 15 > travelGoal.travelPixelsFor(defensePoint, u)
      && u.pixelDistanceTravelling(travelGoal) > 32 * 15)
  }

  private val wallGapsFor = new WallGaps(base)

  override def run(): Unit = {
    vicinity = defensePoint
    if (units.isEmpty) return
    if (emergencyDTHugs()) return

    val defense  = new DefenseState(units, enemies, guardZone, guardChoke)
    val canScour = defense.canScour

    lazy val formationWithdraw  = Formations.disengage(this, Some(defense.travelGoal))
    lazy val formationScour     = Formations.march(this, targets.get.headOption.map(_.pixel).getOrElse(vicinity))
    lazy val formationGuard     = guardChoke.map(_.pixelCenter).orElse(wallToPlug().map(_.center)).map(pixel => new FormationStandard(this, FormationStyleGuard, pixel, Some(guardZone))).getOrElse(formationBastion)
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

    val wallGaps        = wallGapsFor(units)
    val blockZerglings  = With.enemies.exists(_.isZerg) && ! enemies.exists(_.isAny(Zerg.Hydralisk, Zerg.Lurker)) && guardChoke.exists(c => base.metro.ourUnits.exists(u => u.unitClass.isStaticDefense && u.pixelDistanceEdge(c.pixelCenter) < 8 * 32))
    val canWithdraw     = defense.withdrawingUnits >= Math.max(2, 0.25 * units.size) && formationWithdraw.placements.size > units.size * .75
    val canPlugWall     = wallGaps.nonEmpty
    val canPlugEdge     = edgeToPlug().isDefined
    val canGuard        = guardChoke.isDefined && (units.size > 5 || blockZerglings || ! With.enemies.exists(_.isZerg) || With.geography.ourBases.length > 1)
    val targetsUnranked =
      if        (canScour)    defense.scourables
      else if   (canWithdraw) SquadAutomation.unrankedEnRouteTo(this, vicinity).toVector
      else                    enemies.filter(threateningBase)
    setTargets(targetsUnranked.sortBy(_.pixelDistanceTravelling(vicinity)).sortBy(IsWorker).sortBy( ! _.canAttackGround))

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
    } else if (canPlugWall) {
      lastAction = "Plug Wall"
      formations += FormationSimple(FormationStylePlug, formationBastion.placements ++ wallGaps)
      formations += formationBastion
      SquadAutomation.send(this)
      wallGaps.foreach { case (unit, pixel) => intendHoldWallGap(unit, pixel) }
    } else if (canPlugEdge) {
      val plugPixel = edgeToPlug().get._2.get.pixelCenter
      lastAction = "Plug Edge"
      formations += FormationSimple(FormationStylePlug, units.map((_, plugPixel)).toMap)
      units.foreach(_.intend(this)
        .setCanFlee(false)
        .setRedoubt(plugPixel)
        .setTerminus(plugPixel)
        .setAction(Plug(plugPixel)))
    } else if (canGuard) {
      lastAction = "Guard"
      if (targets.exists(_.nonEmpty)) {
        formations += formationScour
      }
      formations += formationGuard
      SquadAutomation.send(this)
      guardWithoutTargets()
      if (blockZerglings) {
        units.foreach(_.intent.setCanFlee(false))
      }
    } else {
      lastAction = "Hold"
      formations += formationBastion
      SquadAutomation.send(this)
      units.foreach(_.intent.setCanFlee(false))
    }
  }

  private def threateningBase(enemy: UnitInfo): Boolean = {
    var output = enemy.zone == base.zone
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

  def intendHoldWallGap(unit: FriendlyUnitInfo, pixel: Pixel): Unit = {
    unit.intend(this)
      .setTargets(Vector.empty)
      .setShouldCorrectXY(false)
      .setCanFlee(false)
      .setRedoubt(pixel)
      .setTerminus(pixel)
      .setStation(pixel)
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
    val assigned    = new UnorderedBuffer[FriendlyUnitInfo]()
    val antiAir     = new UnorderedBuffer[FriendlyUnitInfo](units.view.filter(_.canAttackAir))
    val antiGround  = new UnorderedBuffer[FriendlyUnitInfo](units.view.filter(_.canAttackGround))
    Seq(3, 6, 9).foreach(multiplier => {
      targets.foreach(_.foreach(target => {
        val antiTarget = ?(target.flying, antiAir, antiGround).view.filter(_.canAttack(target))
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
      }))
    })
    (antiAir.view ++ antiGround).distinct.foreach(unit => targets.foreach(_.find(unit.canAttack).foreach(intendScouring(unit, _))))
    guardWithoutTargets()
  }

  private def guardWithoutTargets(): Unit = {
    val excluded = units.filter(unit => targets.flatMap(_.headOption).exists(t => ! unit.canAttack(t))).toSet
    if (excluded.isEmpty) return
    // The engage formation pursues the first target and supersedes intent destinations.
    // Units unable to hit it must pursue their own compatible target or keep guarding.
    formations.indices.dropRight(1).foreach(i => {
      val formation = formations(i)
      formations(i) = new Formation {
        override def style: FormationStyle = formation.style
        override def flanking: Boolean = formation.flanking
        override val placements: Map[FriendlyUnitInfo, Pixel] = formation.placements.filterNot(p => excluded(p._1))
      }
    })
    excluded.filterNot(unit => targets.get.exists(unit.canAttack)).foreach(unit => {
      val where = formationDisengage.flatMap(_(unit)).getOrElse(SquadAutomation.getReturn(unit, this))
      unit.intend(this)
        .setTargets(Vector.empty)
        .setTerminus(where)
        .setRedoubt(where)
        .setStation(where)
    })
  }

  private def isBreaching(enemy: UnitInfo, zone: Zone, choke: Option[Edge]): Boolean = (
    enemy.canAttackGround
      && (enemy.base.exists(b => b == base || b.owner.isUs)
        || With.geography.ourBases.exists(_.townHall.exists(th => enemy.inRangeToAttack(th) && th.injury > 0.5))
        || choke.exists(c => enemy.metro.exists(_.bases.contains(base)) && enemy.pixel.walkableTile.groundPixels(base.heart) < c.pixelCenter.walkableTile.groundPixels(base.heart)))
      && (With.fingerprints.workerRush() || ! IsWorker(enemy))
      && ! zone.edges.exists(edge => enemy.pixelDistanceCenter(edge.pixelCenter) < 64 + edge.radiusPixels))

  private def isScourable(enemy: UnitInfo, defenders: Seq[FriendlyUnitInfo], enemyCanAttackGround: Boolean): Boolean = (
    // Don't get baited by 4-pool scouts
    ! (With.frame < Minutes(4)() && Zerg.Drone(enemy) && With.fingerprints.fourPool())
    // Don't scour what we can't kill
    && (defenders.exists(_.canAttack(enemy)) || ((enemy.cloaked || enemy.burrowed) && defenders.exists(_.unitClass.isDetector)))
    // Don't chase Overlords or floating buildings when there are actual threats nearby
    && ((enemy.unitClass.attacksOrCastsOrDetectsOrTransports && ! Zerg.Overlord(enemy)) || ! enemyCanAttackGround)
    // If we don't really want to fight, wait until they push into the base
    && (enemy.flying || With.blackboard.wantToAttack() || enemy.metro.contains(base.metro)))

  private val enemyHasVision: Cache[Boolean] = new Cache(() => enemies.exists(e => e.flying || e.altitude >= base.heart.altitude || e.orderTarget.filter(_.isFriendly).exists(_.base.contains(base))))


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
      val inOurBase = dts.filter(_.base.exists(_.isOurs))
      val target    = Maff.minBy(inOurBase.map(_.pixel))(_.groundPixels(With.geography.home)).getOrElse(Maff.orElse(With.geography.ourBases, Seq(With.geography.ourMain)).map(_.zone.exitNowOrHeart.center).minBy(_.proximity))
      val pixels    = With.units.ours.filter(u => u.unitClass.isDetector && ! u.flying).map(_.pixel).toSeq ++ Seq(target)
      val pixel     = pixels.minBy(p => dts.map(_.pixelDistanceTravelling(p)).min)
      units.foreach(_.intend(this).setAction(HugAt(pixel)))
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
            to = edgeToPlug().map(_._1.centroid.center.walkablePixel).getOrElse(pixel)
          }
          unit.agent.redoubt.set(to)
          unit.agent.decision.set(to)
          Commander.move(unit)
        }
      }
    }
  }
}
