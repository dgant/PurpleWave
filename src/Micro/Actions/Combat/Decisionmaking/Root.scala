package Micro.Actions.Combat.Decisionmaking

import Lifecycle.With
import Micro.Actions.Action
import Planning.Composition.UnitMatchers.UnitMatchSiegeTank
import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

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
        
    private def distanceToGoal(u: FriendlyUnitInfo): Double = u.pixelDistanceTravelling(unit.agent.destination)
    private val pushSpacing = 32.0 * 5.0
    private val framesToRoot = 12
    
    private lazy val weAreALurker         = Zerg.Lurker.accept(unit)
    private lazy val weAreATank           = UnitMatchSiegeTank.accept(unit)
    private lazy val weAreRooted          = (weAreALurker && unit.burrowed) || unit.is(Terran.SiegeTankSieged)
    private lazy val maxRange             = if (unit.is(Terran.SiegeTankUnsieged)) Terran.SiegeTankSieged.groundRangePixels else unit.pixelRangeGround
    private lazy val ourDistanceToGoal    = distanceToGoal(unit)
    private lazy val rootersInPush        = unit.squadmates.filter(s => unit != s && Zerg.Lurker.accept(s) || UnitMatchSiegeTank.accept(s))
    private lazy val rootersInPushCloser  = rootersInPush.count(distanceToGoal(_) < ourDistanceToGoal + pushSpacing)
    
    private lazy val threatsButNoTargets  = unit.matchups.threats.nonEmpty && unit.matchups.targets.isEmpty
    private lazy val insideNarrowChoke    = unit.zone.edges.exists(e => e.radiusPixels < 32.0 * 4.0 && unit.pixelDistanceCenter(e.pixelCenter) < e.radiusPixels)
    private lazy val beingPickedUp        = unit.agent.toBoard.isDefined
    private lazy val outOfCombat          = unit.matchups.battle.isEmpty
    private lazy val inTheWay             = unit.agent.shovers.nonEmpty
    private lazy val retreating           = ! unit.agent.shouldEngage
    private lazy val protectingBase       = unit.matchups.allies.exists(a => a.unitClass.isBuilding && a.matchups.framesOfSafetyDiffused < unit.matchups.framesOfSafetyDiffused)
    private lazy val insideTurretRange    = unit.matchups.threatsInRange.exists(_.unitClass.isBuilding)
    private lazy val nearFormationPoint   = unit.agent.toForm.exists(unit.pixelDistanceCenter(_) < 32.0 * 6.0)
    private lazy val turretsInRange       = unit.matchups.targetsInRange.exists(_.unitClass.isStaticDefense)
    private lazy val buildingInRange      = unit.matchups.targetsInRange.exists(_.unitClass.isBuilding)
    private lazy val combatTargets        = unit.matchups.targets.filter(_.unitClass.helpsInCombat)
    private lazy val targetsInRange       = combatTargets.filter(t => unit.pixelDistanceEdge(t) < maxRange)
    private lazy val targetsNearingRange  = combatTargets.filter(t => { val p = t.projectFrames(framesToRoot); unit.pixelDistanceEdge(t.pixelStartAt(p), t.pixelEndAt(p)) < maxRange})
    private lazy val girdForCombat        = targetsInRange.nonEmpty || targetsNearingRange.nonEmpty
    private lazy val duckForCover         = weAreALurker && unit.matchups.enemyDetectors.isEmpty && unit.matchups.framesOfSafetyDiffused < framesToRoot
    private lazy val leadingPush          = (rootersInPush.size + 1) / 3 > rootersInPushCloser
    private lazy val destinationFarAway   = unit.pixelDistanceCenter(unit.agent.destination) > 32.0 * 4.0
    
    lazy val mustBeUnrooted = (
          (threatsButNoTargets              )
      ||  (weAreATank && insideNarrowChoke  )
      ||  (beingPickedUp                    )
      ||  (outOfCombat && inTheWay          )
    )
    lazy val mustNotRoot = (
          (retreating && ! protectingBase   )
      ||  (weAreATank && insideTurretRange  )
    )
    lazy val wantsToRoot = (
          (nearFormationPoint               )
      ||  (turretsInRange                   )
      ||  (weAreATank && buildingInRange    )
      ||  (leadingPush                      )
      ||  (girdForCombat                    )
      ||  (duckForCover                     )
    )
    lazy val wantsToUnroot = (
          (mustBeUnrooted                   )
      ||  (destinationFarAway               )
    )
    
    lazy val shouldRoot     = ! mustBeUnrooted && ( ! weAreRooted && wantsToRoot    && ! mustNotRoot)
    lazy val shouldUnroot   =   mustBeUnrooted || (   weAreRooted && wantsToUnroot  && ! wantsToRoot)
  
    override def allowed(unit: FriendlyUnitInfo): Boolean = {
      weAreALurker || weAreATank
    }
    
    override def perform(unit: FriendlyUnitInfo) {
      if (shouldRoot)   root(unit)
      if (shouldUnroot) unroot(unit)
    }
    
    private def root(unit: FriendlyUnitInfo) {
      if (weAreRooted) {
        return
      }
      if (weAreALurker) {
        With.commander.burrow(unit)
      }
      if (weAreATank) {
        With.commander.useTech(unit, Terran.SiegeMode)
      }
    }
  
    private def unroot(unit: FriendlyUnitInfo) {
      if ( ! weAreRooted) {
        return
      }
      if (weAreALurker) {
        With.commander.unburrow(unit)
      }
      if (weAreATank) {
        With.commander.useTech(unit, Terran.SiegeMode)
      }
    }
  }
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = true
  
  override def perform(unit: FriendlyUnitInfo) {
    val rootAction = new RootAction(unit)
    rootAction.delegate(unit)
  }
}
