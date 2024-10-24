package Tactic.Production

import Information.Counting.MacroCounter
import Lifecycle.With
import Macro.Requests.RequestBuildable
import Mathematics.Maff
import Planning.ResourceLocks.{LockCurrency, LockCurrencyFor, LockUnits}
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.UnitCounters.CountOne
import Utilities.UnitFilters.IsAny
import Utilities.UnitPreferences._

class ProduceUnitMorphed(requestArg: RequestBuildable, expectedFramesArg: Int) extends Production {
  setRequest(requestArg, expectedFramesArg)
  val classOutput   : UnitClass     = request.unit.get
  val classInput    : UnitClass     = classOutput.whatBuilds._1
  val currencyLock  : LockCurrency  = new LockCurrencyFor(this, classOutput)
  val morpherLock   : LockUnits     = new LockUnits(this,
    IsAny(classInput, _.friendly.exists(_.buildType == classOutput)),
    PreferTrainerFor(classOutput),
    CountOne)

  var morpher: Option[FriendlyUnitInfo] = None

  def isComplete: Boolean = morpher.exists(t => MacroCounter.countComplete(t)(classOutput) > 0)
  def hasSpent  : Boolean = morpher.exists(m => MacroCounter.countExtant(m)(classOutput) > 0)

  def onUpdate(): Unit = {
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
      morpherLock.matcher = if (morpher.isDefined) morpher.contains else classInput
      morpherLock.acquire()
      morpher = morpherLock.units.headOption
      val shouldHide = Seq(Zerg.Lurker, Zerg.Guardian, Zerg.Devourer).contains(classOutput)
      morpher.foreach(_.intend(this)
        .setTrain(classOutput)
        .setCanFlee(shouldHide)
        .setCanFight( ! shouldHide))
    }

    // TODO: Send Hydras/Mutas somewhere smart soon before they morph based on currency projection
    morpher.foreach(_.setProducer(this))
  }
}
