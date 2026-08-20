package Micro.Actions.Protoss

import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import Mathematics.Maff
import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.Recharge
import Micro.Agency.Commander
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.?
import Utilities.Time.Minutes
import Utilities.UnitFilters.{IsWarrior, IsWorker}

object BeRushingZealot extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    Protoss.Zealot(unit)
    && With.frame < Minutes(8)()
    && unit.agent.destinationFinal.pixel.exists(p => ! p.metro.exists(_.isOurs) && p.proximity < 0.5)
    && unit.team.forall(u => u.warriorsCasters.forall(u => u.isAny(Protoss.Probe, Protoss.Zealot, Protoss.ShieldBattery) || u.pixelDistanceCenter(unit) > 320))
  )

  private val typeStrength = Map(
    (Terran.SCV,            0.4),
    (Terran.Marine,         0.4),
    (Terran.Vulture,        1.0),
    (Terran.Firebat,        0.6),
    (Terran.Bunker,         2.0),

    (Protoss.Probe,         0.3),
    (Protoss.Zealot,        1.0),
    (Protoss.Dragoon,       0.75),
    (Protoss.PhotonCannon,  1.75),

    (Zerg.Drone,            0.3),
    (Zerg.Zergling,         0.35),
    (Zerg.Hydralisk,        0.65),
    (Zerg.SunkenColony,     3.5)
  )

  private def scoreUnit(zealot: FriendlyUnitInfo, u: UnitInfo): Double = {
    val sign        = ?(u.isFriendly, 1, ?(u.isEnemy, -1, 0))
    val proximity   = Math.max(0, 1 - Math.max(0, u.pixelsToGetInRange(zealot) - zealot.matchups.pixelsToTargetRange.getOrElse(0d)) / 196.0)
    val health      = u.totalHealth / u.unitClass.maxTotalHealth.toDouble
    val strength    = typeStrength.getOrElse(u.unitClass, u.unitClass.subjectiveValue / Protoss.Zealot.subjectiveValue)
    val hysteresis  = ?(u.isOurs, ?(u.matchups.threatsInRange.nonEmpty, 1.2, 0.9), 1.0)
    val busyness    = ?(u.gathering || u.constructing, 0.5, 1.0)
    sign * proximity * health * strength * hysteresis * busyness
  }

  override protected def perform(zealot: FriendlyUnitInfo): Unit = {
    val scores = zealot.matchups
      .allUnits
      .filter(_.unitClass.attacksGround)
      .map(scoreUnit(zealot, _))
      .toVector
    val score                 = scores.sum
    val canPotshot            = zealot.readyForAttackOrder && zealot.matchups.targetsInRange.exists(_.canAttack(zealot))
    val hugging               = zealot.matchups.targetsInRange.exists(t => t.canAttack(zealot) && t.pixelRangeAgainst(zealot) > 32)
    val shouldFight           = score > 0 || canPotshot || hugging
    val batteries = With.units.ours
      .filter(b =>
        Protoss.ShieldBattery(b)
        && b.complete
        && b.energy * 2 > zealot.unitClass.maxShields - zealot.shieldPoints
        && (zealot.shieldPoints < ?(zealot.pixelDistanceEdge(b) < 64, zealot.unitClass.maxShields, 10)))
      .toVector
    val battery               = Maff.minBy(batteries)(_.pixelDistanceEdge(zealot))
    zealot.agent.shouldFight  = shouldFight
    zealot.agent.destinationNext.set(With.scouting.enemyMain.map(_.heart.center).getOrElse(With.scouting.enemyHome.center))
    DrawMap.label(f"$score%.2f", zealot.pixel.add(0, 24))

    if (zealot.matchups.targetsInRange.forall( ! _.unitClass.attacksGround)
      && zealot.matchups.threatsInRange.isEmpty
      && battery.isDefined) {
      Commander.rightClick(zealot, battery.get)
    } else if (shouldFight) {
      if (canPotshot) {
        attack(zealot, zealot.matchups.targetsInRange.minBy(_.totalHealth))
      } else {
        val targets = With.units.enemy.filter(t => t.unitClass.attacksGround && t.visible)
        if (targets.isEmpty) {
          Commander.move(zealot)
        } else {
          val trappedDragoons = targets.filter(t =>
            Protoss.Dragoon(t)
              && t.tile.adjacent4.count(_.walkable) < 2
              && t.inRangeToAttack(zealot)
              && t.matchups.targetsInRange.exists(b => b.unitClass.isBuilding && t.pixelDistanceEdge(b) < 16))
          if (trappedDragoons.nonEmpty && false) { // Disabled since we aren't doing Pylon traps
            Maff.minBy(trappedDragoons)(_.pixelDistanceEdge(zealot)).foreach(attack(zealot, _))
          } else {
            val targets = zealot.matchups.targets
              .filter(_.canAttack(zealot))
              .filter(_.visible)
              .filter(t => ! IsWorker(t) || t.matchups.targetsInRange.nonEmpty || t.constructing || t.gathering || zealot.pixelsToGetInRange(t) < 16)
            if (targets.nonEmpty) {
              val target = targets.minBy(t => t.totalHealth + 2 * zealot.pixelsToGetInRange(t))
              if (zealot.readyForAttackOrder || ! target.canMove) {
                attack(zealot, target)
              } else if (target.isAny(Protoss.Zealot, Zerg.Zergling, IsWorker) && target.presumptiveTarget.contains(zealot) && target.inRangeToAttack(zealot)) {
                retreat(zealot)
              } else if (target.speedApproaching(zealot) < 0) {
                zealot.agent.destinationNext.set(target.projectFrames(zealot.cooldownLeft))
                Commander.move(zealot)
              } else {
                attack(zealot, target)
              }
            }
          }
        }
      }
      Commander.move(zealot)
    } else {
      retreat(zealot)
    }
  }

  def attack(zealot: FriendlyUnitInfo, target: UnitInfo): Unit = {
    zealot.agent.toAttack = Some(target)
    Commander.attack(zealot)
  }

  def retreat(zealot: FriendlyUnitInfo): Unit = {
    if (zealot.matchups.pixelsToThreatRange.isEmpty) return

    Recharge(zealot)

    val allies  = zealot.matchups.allies.filter(IsWarrior).filter(_.matchups.pixelsToThreatRange.exists(_ > zealot.matchups.pixelsToThreatRange.get + 64))
    val home    = Maff.minBy(allies)(_.matchups.pixelsToThreatRange)
      .map(_.pixel)
      .orElse(Maff.minBy(With.units.ours.filter(Protoss.Gateway).map(_.pixel))(zealot.pixelDistanceTravelling))
      .getOrElse(With.geography.home.center)

    zealot.agent.destinationNext.set(home)
    Commander.move(zealot)
  }
}
