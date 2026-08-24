package Micro.Actions.Combat.Tactics

import Debugging.Visualizations.Forces
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Physics.Force
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Actions.Scouting.Scout
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import Micro.Heuristics.Potential
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Strategery.Strategies.AllRaces.{WorkersKill, WorkersRaze, WorkersSpread, WorkersUnite}
import Utilities.?
import Utilities.UnitFilters.{IsBuilding, IsWarrior, IsWorker}

object Tickle2026 extends Action {

  override def allowed(tickler: FriendlyUnitInfo): Boolean = IsWorker(tickler)

  def workersSpread : Boolean = WorkersSpread.activate()
  def workersUnite  : Boolean = WorkersUnite.activate()
  def workersRaze   : Boolean = WorkersRaze.activate()
  def workersKill   : Boolean = WorkersKill.activate()

  override protected def perform(tickler: FriendlyUnitInfo): Unit = {

    // Return resources now so we can mine again later
    if (tickler.carrying && tickler.base.exists(_.owner.isUs)) {
      Commander.returnCargo(tickler)
      return
    }

    tickler.agent.toAttack =
      Maff.minBy(
        Maff.orElse(
          tickler.matchups.threats.filter(Terran.Vulture),
          tickler.matchups.threats.filter(Terran.Marine),
          tickler.matchups.threats.filter(Protoss.Dragoon),
          tickler.matchups.threats.filter(Protoss.PhotonCannon),
          tickler.matchups.threats.filter(Protoss.Zealot).filter(x => tickler.totalHealth > 16),
          tickler.matchups.threats.filter(Zerg.Zergling).filter(x => tickler.totalHealth > 10)))(_.totalHealth)
    Commander.attack(tickler)

    val strengthGlobalUs    = With.units.ours   .filter(IsWorker).map(strengthGlobal).sum
    val strengthGlobalFoe   = With.units.enemy  .filter(IsWorker).map(strengthGlobal).sum
    val strengthGlobalRatio = strengthGlobalUs / Math.max(1.0, strengthGlobalFoe)
    val strength            = tickler.matchups.allUnits.filter(_.unitClass.attacksGround).map(u => strengthLocal(tickler, u) * Maff.or1(strengthGlobalRatio, u.isFriendly)).sum

    if (strength > 0 && tickler.totalHealth > 10) {
      fight(tickler)
    } else if (tickler.matchups.threatsInPixels(128 - tickler.totalHealth).nonEmpty) {
      flee(tickler)
    }
    arson(tickler)
    Commander.move(tickler)
  }

  private def strengthGlobal(unit: UnitInfo): Double = {
    unit.totalHealth
  }

  private def strengthLocal(tickler: FriendlyUnitInfo, u: UnitInfo): Double = {
    val sign        = ?(u.isFriendly, 1, ?(u.isEnemy, -1, 0))
    val proximity   = Math.max(0, 1 - Math.max(0, u.pixelsToGetInRange(tickler) - tickler.matchups.pixelsToTargetRange.getOrElse(0d)) / 320.0)
    val health      = u.totalHealth / u.unitClass.maxTotalHealth.toDouble
    val fighting    = u.matchups.allies.filter(IsWarrior).exists(_.matchups.threatsInRange.nonEmpty)
    val lowAllyHP   = Maff.min(tickler.matchups.allies.filter(Protoss.Zealot).filter(_.matchups.threatsInPixels(8).nonEmpty).map(_.totalHealth.toDouble)).getOrElse(1000.0)
    val assisting   = Maff.clamp(tickler.totalHealth / Math.max(1.0, lowAllyHP), 1.0, 2.0)
    val hysteresis  = ?(fighting, 1.2, 0.7)
    val busyness    = ?(u.gathering || u.constructing, 1.0, 1.0)
    val strength    = ?(Terran.SCV(tickler), 1.35, 1.0)
    val output      = sign * proximity * health * hysteresis * busyness * assisting * strength
    output
  }

  def fight(tickler: FriendlyUnitInfo): Unit = {
    val targets = With.units.enemy.filter(t =>
      t.unitClass.attacksGround
      && t.visible
      && (t.zone == tickler.zone || tickler.pixelsToGetInRange(t) < 256)
      && ( ! IsWorker(t) || t.matchups.targetsInRange.nonEmpty || t.constructing || t.gathering || tickler.pixelsToGetInRange(t) < 16))
    val target = Maff.minBy(targets)(t => t.totalHealth + 1.0 * tickler.pixelsToGetInRange(t))
    tickler.agent.toAttack = target
    if (target.isDefined
      && ! Terran.SCV(tickler)
      && target.exists(Terran.SCV)
      && ! tickler.readyForAttackOrder
      && tickler.framesToConnectDamage(target.get) > tickler.framesToBeReadyForAttackOrder) {
      flee(tickler)
    }
    Commander.attack(tickler)
  }

  def arson(tickler: FriendlyUnitInfo): Unit = {
    val targets = With.units.enemy
      .filter(IsBuilding)
      .map(b => (b, Maff.minBy(b.tileArea.tilesSurrounding.filter(t => t.walkable && t.enemyRangeGround <= 0))(tickler.pixelDistanceSquared)))
      .filter(_._2.isDefined)
      .map(b => (b._1, b._2.get.center))
      .toVector
    if (targets.isEmpty) {
      if (tickler.matchups.threatsInPixels(256 - 128 * tickler.totalHealth / tickler.unitClass.maxTotalHealth).nonEmpty) {
        flee(tickler)
      } else {
        Scout(tickler)
      }
    } else {
      val target    = targets.minBy(t => t._1.totalHealth - 0.5 * Maff.or0(t._1.unitClass.maxTotalHealth, t._1.complete) - Maff.or0(1000, t._1.isAny(Terran.Barracks, Terran.Factory, Protoss.Gateway, Protoss.PhotonCannon, Zerg.SunkenColony, Zerg.CreepColony, Zerg.SpawningPool)))
      val pixel     = target._2
      val distance  = tickler.pixelDistanceTravelling(pixel)
      if (distance > 80) {
        tickler.agent.decision.set(pixel)
        Commander.move(tickler)
      } else {
        tickler.agent.toAttack = Some(target._1)
        Commander.attack(tickler)
      }
    }
  }

  def flee(tickler: FriendlyUnitInfo): Unit = {
    tickler.agent.home.set(tickler.agent.destinationFinal.pixel)
    Retreat(tickler)
    val mineral = With.geography.ourMain.minerals.headOption.getOrElse(With.units.ours.filter(IsBuilding).head)
    val away    = Potential.softAvoidThreatRange(tickler)
    val out     = MicroPathing.getGroundWaypointToPixel(tickler.pixel, mineral.pixel)
    val awayN   = away.normalize
    val outN    = new Force(out.subtract(tickler.pixel)).normalize
    if (awayN.dot(outN) > 0.7) {
      Commander.rightClick(tickler, mineral)
    } else {
      tickler.agent.forces.put(Forces.threat, away)
      tickler.agent.forces.put(Forces.travel, Potential.towards(tickler, tickler.agent.destinationFinal.pixel.get.zone.centroid.center))
      tickler.agent.forces.put(Forces.spacing, Potential.preferSpacing(tickler))
      MicroPathing.moveForcefully(tickler)
    }
  }
}
