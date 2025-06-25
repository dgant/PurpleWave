package Micro.Actions.Combat.Decisionmaking

import Lifecycle.With
import Micro.Actions.Action
import Micro.Targeting.Target
import Micro.Agency.Commander
import Micro.Targeting.FiltersSituational.TargetFilterVisibleInRange
import ProxyBwapi.Orders
import Utilities.UnitFilters.IsTank
import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Root extends Action {
  
  private class RootAction(unit: FriendlyUnitInfo) extends Action {
    // Be unrooted if there are threats but no targets
    // Be unrooted if we are a tank inside a narrow choke
    // Be unrooted if being picked up
    // Be unrooted if out of combat and in the way
    // Don't root if retreating and not protecting anything
    // Don't root if we are a tank in range of static defense
    // Do root if near formation point
    // Do root if in range to attack static defense
    // Do root if we're a tank in range to attack enemies (potentially after they charge at us)
    // Do root if we're a tank in the closest third of our squad's rooters to the destination
    // Do unroot if our destination is far away
        
    private def distanceToGoal(u: FriendlyUnitInfo): Double = u.pixelDistanceTravelling(unit.agent.destinationFinal())
    private val pushSpacing = 32.0 * 3.0
    private def framesToRoot = 18 + With.reaction.agencyAverage
    
    private lazy val weAreALurker           = Zerg.Lurker(unit)
    private lazy val weAreATank             = IsTank(unit)
    private lazy val weAreRooted            = (weAreALurker && unit.burrowed) || unit.is(Terran.SiegeTankSieged)
    private lazy val maxRange               = if (unit.is(Terran.SiegeTankUnsieged)) Terran.SiegeTankSieged.pixelRangeGround else unit.pixelRangeGround
    private lazy val ourDistanceToGoal      = distanceToGoal(unit)
    private lazy val rootersInPush          = unit.alliesSquad.filter(s => unit != s && Zerg.Lurker(s) || IsTank(s))
    private lazy val rootersInPushCloser    = rootersInPush.count(u => distanceToGoal(u) < ourDistanceToGoal + pushSpacing)

    def notHiddenUphill(target: UnitInfo): Boolean = target.visible || target.altitude <=  unit.altitude
    private lazy val visibleTargets         = unit.matchups.targets.filter(notHiddenUphill)
    private lazy val visibleTargetsInRange  = unit.matchups.targetsInRange.filter(notHiddenUphill)
    private lazy val threatsButNoTargets    = unit.matchups.threats.nonEmpty && visibleTargets.isEmpty
    private lazy val insideNarrowChoke      = unit.zone.edges.exists(e => e.radiusPixels < 32 * 4 && unit.pixelDistanceCenter(e.pixelCenter) < e.radiusPixels)
    private lazy val beingPickedUp          = unit.agent.toBoard.isDefined
    private lazy val outOfCombat            = unit.battle.isEmpty
    private lazy val inTheWay               = With.coordinator.pushes.get(unit).exists(p => p.force(unit).isDefined)
    private lazy val retreating             = ! unit.agent.shouldFight
    private lazy val protectingBase         = unit.alliesBattle.exists(a => a.unitClass.isBuilding && a.matchups.framesOfSafety < unit.matchups.framesOfSafety)
    private lazy val insideTurretRange      = unit.matchups.threatsInRange.exists(_.unitClass.isBuilding)
    private lazy val nearFormationPoint     = unit.agent.station.pixel.exists(_.pixelDistance(unit.pixel) < 24)
    private lazy val turretsInRange         = visibleTargetsInRange.exists(_.unitClass.isStaticDefense)
    private lazy val buildingInRange        = visibleTargetsInRange.exists(_.unitClass.isBuilding)
    private lazy val combatTargets          = unit.matchups.enemies.filter(e => unit.canAttack(e) && e.unitClass.attacksOrCastsOrDetectsOrTransports)
    private lazy val targetsInRange         = combatTargets.filter(t => unit.pixelDistanceEdge(t) < maxRange)
    private lazy val targetsNearingRange    = combatTargets.filter(t => { val p = t.projectFrames(framesToRoot); unit.pixelDistanceEdge(t.pixelStartAt(p), t.pixelEndAtExclusive(p)) < maxRange - 32})
    private lazy val girdForCombat          = targetsInRange.nonEmpty || targetsNearingRange.size > 3
    private lazy val artilleryOutOfRange    = unit.matchups.targets.filter(t => t.canAttack(unit) && t.pixelRangeAgainst(unit) >unit.pixelRangeAgainst(t) && t.inRangeToAttack(unit))
    private lazy val duckForCover           = false && (weAreALurker && unit.matchups.groupVs.detectors.isEmpty && unit.matchups.framesOfSafety < framesToRoot && (artilleryOutOfRange.isEmpty || ! With.enemy.isTerran)) // Root for safety, but not in range of Tanks if they can scan us
    private lazy val letsKillThemAlready    = weAreALurker && unit.agent.toAttack.exists(_.pixelDistanceEdge(unit) < 64)
    private lazy val destinationFarAway     = unit.pixelDistanceCenter(unit.agent.destinationFinal()) > 32 * 4 && ! nearFormationPoint
    private lazy val hugged                 = weAreATank && unit.matchups.threats.exists(t => ! t.flying && t.pixelDistanceEdge(unit) <= 96) && unit.matchups.targets.nonEmpty && unit.matchups.targets.forall(_.pixelDistanceEdge(unit) <= 96)
    
    lazy val mustBeUnrooted: Boolean = (
          (threatsButNoTargets              )
      ||  (weAreATank && insideNarrowChoke  )
      ||  (beingPickedUp                    )
      ||  (outOfCombat && inTheWay          )
      ||  hugged
    )
    lazy val mustNotRoot: Boolean = (
          (retreating && ! protectingBase   )
      ||  (weAreATank && insideTurretRange  )
    )
    lazy val wantsToRoot: Boolean = (
          (nearFormationPoint               )
      ||  (turretsInRange                   )
      ||  (weAreATank && buildingInRange    )
      ||  (girdForCombat                    )
      ||  (duckForCover                     )
      ||  (letsKillThemAlready              )
    )
    lazy val wantsToUnroot: Boolean = (
          (mustBeUnrooted                   )
      ||  (destinationFarAway               )
    )
    
    lazy val shouldRoot   : Boolean = ! mustBeUnrooted && ( ! weAreRooted && wantsToRoot    && ! mustNotRoot)
    lazy val shouldUnroot : Boolean =   mustBeUnrooted || (   weAreRooted && wantsToUnroot  && ! wantsToRoot)
  
    override def allowed(unit: FriendlyUnitInfo): Boolean = (
      (weAreALurker || (weAreATank && With.self.hasTech(Terran.SiegeMode)))
      && unit.order != Orders.Sieging
      && unit.order != Orders.Unsieging
      && unit.order != Orders.Burrowing
      && ! unit.morphing
    )
    
    override def perform(unit: FriendlyUnitInfo): Unit = {
      Target.choose(unit, TargetFilterVisibleInRange)
      if (shouldRoot)   root(unit)
      if (shouldUnroot) unroot(unit)
    }
    
    private def root(unit: FriendlyUnitInfo): Unit = {
      if (weAreRooted) {
        return
      }
      if (weAreALurker) {
        Commander.burrow(unit)
      }
      if (weAreATank) {
        Commander.useTech(unit, Terran.SiegeMode)
      }
    }
  
    private def unroot(unit: FriendlyUnitInfo): Unit = {
      if ( ! weAreRooted) {
        return
      }
      if (weAreALurker) {
        Commander.unburrow(unit)
      }
      if (weAreATank) {
        Commander.useTech(unit, Terran.SiegeMode)
      }
    }
  }
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.isAny(IsTank,  Zerg.Lurker)
  
  override def perform(unit: FriendlyUnitInfo): Unit = {
    val rootAction = new RootAction(unit)
    rootAction.delegate(unit)
  }
}
