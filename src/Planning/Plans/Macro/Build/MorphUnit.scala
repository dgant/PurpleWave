package Planning.Plans.Macro.Build

import Lifecycle.With
import Macro.Buildables.{Buildable, BuildableUnit}
import Macro.Scheduling.MacroCounter
import Mathematics.Maff
import Micro.Agency.Intention
import Planning.ResourceLocks.{LockCurrency, LockCurrencyForUnit, LockUnits}
import Planning.UnitCounters.CountOne
import Planning.UnitMatchers.{MatchMorphingInto, MatchOr, MatchSpecific}
import Planning.UnitPreferences._
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo


class MorphUnit(val classToMorph: UnitClass) extends Production {

  override def producerCurrencyLocks: Seq[LockCurrency] = Seq(currencyLock)
  override def buildable: Buildable = BuildableUnit(classToMorph)
  
  val currencyLock  = new LockCurrencyForUnit(classToMorph)
  val morpherClass  = classToMorph.whatBuilds._1
  val morpherLock   = new LockUnits(this)
  morpherLock.matcher = MatchOr(morpherClass, MatchMorphingInto(classToMorph))
  morpherLock.counter = CountOne
  
  private var morpher: Option[FriendlyUnitInfo] = None
  
  override def isComplete: Boolean = morpher.exists(t => MacroCounter.countComplete(t)(classToMorph) > 0)
  
  override def onUpdate() {

    // Claim an in-progress but unmanaged morphing unit, to avoid duplicating production
    // Shared somewhat with BuildBuilding
    lazy val alreadyMorphing = With.units.ours.filter(u =>
      MacroCounter.countCompleteOrIncomplete(u)(classToMorph) > 0
      && MacroCounter.countComplete(u)(classToMorph) == 0
      && u.getProducer.forall(p => p == this || ! With.prioritizer.isPrioritized(p)))
    morpher = morpher
      .filter(m => m.alive && (m.is(morpherClass) || MacroCounter.countCompleteOrIncomplete(m)(classToMorph) > 0))
      .orElse(Maff.minBy(alreadyMorphing)(_.frameDiscovered))
  
    // Shared somewhat with TrainUnit
    currencyLock.framesPreordered = (
      classToMorph.buildUnitsEnabling.map(With.projections.unit)
      :+ With.projections.unit(morpherClass)).max
    
    currencyLock.isSpent = morpher.exists(m => MacroCounter.countCompleteOrIncomplete(m)(classToMorph) > 0)
    currencyLock.acquire(this)
    if (currencyLock.satisfied && ! currencyLock.isSpent) {
      setPreference()
      morpherLock.matcher = morpher.map(m => new MatchSpecific(Set(m))).getOrElse(morpherClass)
      morpherLock.acquire(this)
      morpher = morpherLock.units.headOption
      morpher.foreach(_.agent.intend(this, new Intention {
        toTrain = Some(classToMorph)
        // TODO: Include behavior for morphing Guardians/Devourers
        canFlee = classToMorph == Zerg.Lurker
        canFight = classToMorph != Zerg.Lurker
      }))
    }
    // TODO: Send Hydras/Mutas somewhere smart soon before they morph based on currency projection
    morpher.foreach(_.setProducer(this))
  }
  
  protected def setPreference() {
    if (classToMorph.isWorker) {
      morpherLock.preference = PreferAll(PreferHatcheryWithThreeLarva, PreferBaseWithFewerWorkers)
    } else if (morpherClass == Zerg.Larva) {
      morpherLock.preference = PreferAll(PreferHatcheryWithThreeLarva, PreferBaseWithMoreWorkers)
    } else {
      morpherLock.preference = u => (if (u.visibleToOpponents) 0 else 1) - u.matchups.framesOfSafety
    }
  }
}
