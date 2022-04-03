package Tactics.Production

import Lifecycle.With
import Macro.Buildables.RequestProduction
import Macro.Scheduling.MacroCounter
import Mathematics.Maff
import Micro.Agency.Intention
import Planning.ResourceLocks.{LockCurrency, LockCurrencyFor, LockUnits}
import Planning.UnitCounters.CountOne
import Planning.UnitMatchers.{MatchMorphingInto, MatchOr, MatchSpecific}
import Planning.UnitPreferences._
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class MorphUnit(val buildableUnit: RequestProduction) extends Production {

  setBuildable(buildableUnit)
  val classOutput  : UnitClass      = buildable.unit.get
  val classInput    : UnitClass     = classOutput.whatBuilds._1
  val currencyLock  : LockCurrency  = new LockCurrencyFor(this, classOutput, 1)
  val morpherLock   : LockUnits     = new LockUnits(this)
  morpherLock.matcher = MatchOr(classInput, MatchMorphingInto(classOutput))
  morpherLock.counter = CountOne
  morpherLock.preference = PreferTrainerFor(classOutput)

  var morpher: Option[FriendlyUnitInfo] = None

  def isComplete: Boolean = morpher.exists(t => MacroCounter.countComplete(t)(classOutput) > 0)
  def hasSpent: Boolean = morpher.exists(m => MacroCounter.countExtant(m)(classOutput) > 0)

  def onUpdate() {

    // Claim an in-progress but unmanaged morphing unit, to avoid duplicating production
    // Shared somewhat with BuildBuilding
    lazy val alreadyMorphing = With.units.ours.filter(u =>
      MacroCounter.countExtant(u)(classOutput) > 0
      && MacroCounter.countComplete(u)(classOutput) == 0
      && u.producer.forall(p => p == this || ! p.isPrioritized))
    morpher = morpher
      .filter(m => m.alive && (m.is(classInput) || MacroCounter.countExtant(m)(classOutput) > 0))
      .orElse(Maff.minBy(alreadyMorphing)(_.frameDiscovered))

    // Shared somewhat with TrainUnit
    if (hasSpent || currencyLock.acquire()) {
      morpherLock.matcher = morpher.map(m => new MatchSpecific(Set(m))).getOrElse(classInput)
      morpherLock.acquire()
      morpher = morpherLock.units.headOption
      morpher.foreach(_.intend(this, new Intention {
        toTrain = Some(classOutput)
        // TODO: Include behavior for morphing Guardians/Devourers
        canFlee = classOutput == Zerg.Lurker
        canFight = classOutput != Zerg.Lurker
      }))
    }

    // TODO: Send Hydras/Mutas somewhere smart soon before they morph based on currency projection
    morpher.foreach(_.setProducer(this))
  }
}
