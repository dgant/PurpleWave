package Macro.Scheduling

import Debugging.SimpleString
import Lifecycle.With
import Macro.Requests._
import Mathematics.Maff
import ProxyBwapi.Buildable
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.Techs
import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}
import ProxyBwapi.UnitInfo.UnitInfo
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}
import Utilities.Time.{Forever, GameTime}
import Utilities.{?, CloneyCountMap}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

final class MacroSim {
  trait InsertionResult extends SimpleString
  object Success                extends InsertionResult
  object PrecedesMinInsert      extends InsertionResult
  object MissingMinerals        extends InsertionResult
  object MissingGas             extends InsertionResult
  object MissingSupply          extends InsertionResult
  object MissingUpgrade         extends InsertionResult
  object MissingTech            extends InsertionResult
  object MissingProducer        extends InsertionResult
  object MissingUnitRequirement extends InsertionResult
  object StealsAddon            extends InsertionResult
  object StealsProducer         extends InsertionResult
  object StealsMinerals         extends InsertionResult
  object StealsGas              extends InsertionResult
  object StealsSupply           extends InsertionResult
  object TooFarInTheFuture      extends InsertionResult

  val redundant = new ArrayBuffer[RequestBuildable]() // We don't need this for bot operation; it's just here for debugging
  val denied    = new ArrayBuffer[(RequestBuildable, InsertionResult)]()
  val steps     = new mutable.ArrayBuffer[MacroStep]()
  val minInsert = new mutable.OpenHashMap[RequestBuildable, Int]()

  private def _queue: Seq[(RequestBuildable, Int)] = steps.view.filter(_.request.isDefined).map(s => (s.request.get, s.event.dFrames))
  def queue: Seq[(RequestBuildable, Int)] = _queue.filter(_._1.specificTrainee.isDefined) ++ _queue.filter(_._1.specificTrainee.isEmpty)

  private var _simIncomeMineralsPerFrame: Double = _
  private var _simIncomeGasPerFrame: Double = _
  private var _autosupply: Boolean = _

  private def trulyUnoccupied(unit: UnitInfo): Boolean = unit.complete && (unit.remainingOccupationFrames == 0 || unit.isAny(Protoss.Reaver, Protoss.Carrier))
  def simulate(): Unit = {
    _simIncomeMineralsPerFrame = With.accounting.ourIncomePerFrameMinerals
    _simIncomeGasPerFrame = ?(With.gathering.gasIsCappedOnQuantity, With.accounting.ourIncomePerFrameGasMax, With.accounting.ourIncomePerFrameGas)
    _autosupply = false

    redundant.clear()
    denied.clear()
    steps.clear()
    minInsert.clear()

    val initialStep = new MacroStep
    val initialState = initialStep.state
    initialState.minerals           = With.self.minerals
    initialState.gas                = With.self.gas
    initialState.supplyAvailable    = Math.min(400, With.units.ours.filter(u => u.complete || u.isAny(Zerg.Lair, Zerg.Hive)).map(_.unitClass.supplyProvided).sum)
    initialState.supplyUsed         = With.units.ours.map(_.unitClass.supplyRequired).sum
    initialState.mineralPatches     = With.self.bases.view.map(_.minerals.count(_.mineralsLeft >= 8)).sum
    initialState.geysers            = With.self.bases.view.map(_.gas.count(g => g.isOurs && g.complete && g.gasLeft > 0)).sum
    initialState.unitsExtant        = new CloneyCountMap(With.macroCounts.oursExtant)
    initialState.unitsComplete      = new CloneyCountMap(With.macroCounts.oursComplete)
    initialState.unitsCompleteASAP  = new CloneyCountMap(With.macroCounts.oursComplete)
    Upgrades.all.foreach(u => initialState.upgrades(u) = With.self.getUpgradeLevel(u))
    initialState.techs ++= Techs.all.view.filter(With.self.hasTech)
    insert(initialStep)

    /////////////////////////////////////////////////
    // Construct events for production in progress //
    /////////////////////////////////////////////////

    With.units.ours
      .filter(u => u.unitClass.isHatchlike && u.openForBusiness)
      .foreach(hatch => larvaSteps(hatch.completionFrame % 342, ASAP = true).foreach(insert))

    With.units.ours
      .filter(trulyUnoccupied)
      .foreach(u => initialState.producers(u.unitClass) += 1)

    With.units.ours
      .filterNot(trulyUnoccupied)
      .foreach(u => {
        val step      = new MacroStep
        val event     = step.event
        event.dFrames = u.remainingOccupationFrames
        val finalClass =
          if (u.isAny(Zerg.Egg, Zerg.Cocoon, Zerg.LurkerEgg)) {
            u.buildType
          } else {
            u.unitClass
          }
        if (u.morphing || ! u.complete) {
          step.request = Some(RequestUnit(finalClass, initialState.unitsExtant(u.unitClass), specificTraineeArg = Some(u)))
          event.dUnitComplete       = finalClass
          event.dUnitCompleteN      = finalClass.copiesProduced
          event.dUnitCompleteASAP   = finalClass
          event.dUnitCompleteASAPN  = finalClass.copiesProduced
          event.dProducer1          = finalClass
          event.dProducer1N         = finalClass.copiesProduced
          if (u.isNone(Zerg.Lair, Zerg.Hive)) {
            event.dSupplyAvailable += finalClass.supplyProvided
          }
        }
        if (u.upgrading) {
          step.request = Some(RequestUpgrade(u.upgradingType, 1 + With.self.getUpgradeLevel(u.upgradingType)))
          event.dUpgrade = u.upgradingType
          event.dUpgradeLevel = 1 + initialState.upgrades(u.upgradingType)
        } else if (u.teching) {
          step.request = Some(RequestTech(u.techingType))
          event.dTech = u.techingType
        } else if (finalClass.isGas) {
          event.dGeysers += 1
        } else if ( ! u.complete && u.unitClass.isResourceDepot && u.isNone(Zerg.Lair, Zerg.Hive)) {
          val base = u.base.filter(_.townHall.contains(u))
          base.foreach(b => event.dMineralPatches += b.minerals.count(_.mineralsLeft >= 8))
        }
        event.dProducer1  = finalClass
        event.dProducer1N = Maff.fromBoolean(! u.morphing)
        insert(step)
      })
      if (With.self.isTerran) {
        val addons = With.units.ours.filter(_.isAny(Terran.MachineShop, Terran.ControlTower))
        addons.foreach(addon => addon.addonOf.foreach(parent => {
          val substitute  = AddonSubstitution.fromReal(addon.unitClass)
          initialState.unitsExtant(substitute) += 1
          if (addon.complete && trulyUnoccupied(parent)) {
            initialState.unitsComplete(substitute) += 1
            initialState.producers(AddonSubstitution.fromReal(addon.unitClass)) += 1
          }
        }))
        addons.foreach(addon =>addon.addonOf.foreach(parent => {
          if ( ! addon.complete) {
            val substitute  = AddonSubstitution.fromReal(addon.unitClass)
            val step        = new MacroStep
            val event       = step.event
            step.request = Some(RequestUnit(substitute, initialState.unitsExtant(substitute), specificTraineeArg = Some(addon)))
            event.dFrames             = addon.remainingCompletionFrames
            event.dUnitComplete       = substitute
            event.dUnitCompleteN      = 1
            event.dUnitCompleteASAP   = substitute
            event.dUnitCompleteASAPN  = 1
            event.dAddon              = substitute
            event.dAddonN             = 1
            insert(step)
          } else if ( ! trulyUnoccupied(parent)) {
            val substitute  = AddonSubstitution.fromReal(addon.unitClass)
            val step        = new MacroStep
            val event       = step.event
            event.dFrames   = parent.remainingOccupationFrames
            event.dAddon    = substitute
            event.dAddonN   = 1
            insert(step)
          }
        }))
      }

    // Populate states as of each event
    updateStatesFrom(1)

    // Insert requests
    val requests = new ArrayBuffer[RequestBuildable]
    With.scheduler.requests.view
      .map(_.request)
      .filterNot(r => With.blackboard.toCancel().contains(r.buildable))
      .foreach(request => {
        var include: Boolean = false
        include ||= request.upgrade.exists(u => steps.last.state.upgrades(u) < request.quantity)
        include ||= request.tech.exists(t => ! steps.last.state.techs.contains(t))
        include ||= request.unit.isDefined
        include ||= request == RequestAutosupply
        if (include) {
          requests += request
        }
      })

    // For each request, check if we've satisfied it by the end, and if not, insert it
    requests.foreach(satisfyRequest)
  }

  private val maximumFramesAhead = GameTime(3, 30)() // The slowest thing in the game is
  private def satisfyRequest(request: RequestBuildable): Unit = {
    if (request == RequestAutosupply) {
      if ( ! _autosupply) {
        minInsert.clear()
      }
      _autosupply = true
      return
    }
    tryInsertRequest(request)
  }

  private def tryInsertRequest(request: RequestBuildable): Unit = {
    // TODO: Don't occupy Probes
    // TODO: Don't return producers which morph
    // TODO: Reduce diff for zergling/scourge

    // Units:
    // - (Default) If there is no placement query, use our state count
    // - (Special) If there is a  placement query, count our complete units which the tile filter accepts
    var unitDiff = request.unit
      .map(u =>
        request.quantity -
          ?(request.placement.isEmpty,
            ?(request.minStartFrame <= 0,
              steps.last.state.unitsCompleteASAP(u),
              extantBy(request.buildable, request.minStartFrame)),
            With.units.ours
              .filter(u)
              .map(_.tileTopLeft)
              .count(request.placement.get.acceptExisting)))
      .getOrElse(0)
    if (request.unit.exists(_.isTwoUnitsInOneEgg)) {
      unitDiff = (unitDiff + 1) / 2
    }
    val upgradeDiff = request.upgrade.map(u => request.quantity - steps.last.state.upgrades(u)).getOrElse(0)
    val techDiff    = Maff.fromBoolean(request.tech.exists(t => ! steps.last.state.techs.contains(t)))
    val diff        = Maff.vmax(unitDiff, upgradeDiff, techDiff)
    if (diff <= 0) {
      redundant += request
      return
    }

    var allowSupplyInjection: Boolean = true
    var iDiff = 0
    while (iDiff < diff) {
      iDiff += 1
      var insertionResult : InsertionResult = TooFarInTheFuture
      var stepIndex       : Int             = minInsert.getOrElse(request, 0)
      var succeeded       : Boolean         = false
      var stepBefore      : MacroStep       = null
      while ( ! succeeded && stepIndex < steps.length && steps(stepIndex).event.dFrames < maximumFramesAhead) {
        stepBefore      = steps(stepIndex)
        insertionResult = tryInsertAfter(request, stepBefore, stepIndex)
        succeeded       = insertionResult == Success
        if ( ! succeeded) {
          minInsert(request) = Math.max(stepIndex, minInsert.getOrElse(request, stepIndex))
          stepIndex += 1
        }
      }
      if ( ! succeeded) {
        denied += ((request, insertionResult))
        return
      }
      val framesAfter = Math.ceil(Seq(
        request.minStartFrame - With.frame,
        stepBefore.event.dFrames,
        if (request.mineralCost <= stepBefore.state.minerals) 0 else stepBefore.event.dFrames + Maff.nanToN((request.mineralCost  - stepBefore.state.minerals)  / _simIncomeMineralsPerFrame,  Forever()),
        if (request.gasCost     <= stepBefore.state.gas)      0 else stepBefore.event.dFrames + Maff.nanToN((request.gasCost      - stepBefore.state.gas)       / _simIncomeGasPerFrame,       Forever()))
        .max).toInt
      if (framesAfter < maximumFramesAhead) {
        val (stepStart, stepFinish, stepsExtra) = constructStepsForRequest(request, framesAfter)
        lazy val indexAfterSupplyWouldFinish  = fit(stepStart.event.dFrames + Terran.SupplyDepot.buildFrames)
        lazy val stepAfterSupplyWouldFinish   = steps(Math.min(indexAfterSupplyWouldFinish, steps.length - 1))

        if (_autosupply
          && allowSupplyInjection
          && stepStart.event.dSupplyUsed > 0
          && stepAfterSupplyWouldFinish.state.supplyAvailable < Math.min(
            400,
            stepBefore.state.supplyUsed
            + stepStart.event.dSupplyUsed
            + stepBefore.state.supplyUsePerFrame * Terran.SupplyDepot.buildFrames / 2)) {

          val
          farm = With.self.supplyClass
          tryInsertRequest(RequestUnit(farm, stepBefore.state.unitsExtant(farm) + 1, With.frame + stepBefore.event.dFrames - With.self.supplyClass.buildFrames))
          iDiff -= 1
          allowSupplyInjection = false
        } else {
          val iStart = insert(stepStart)
          insert(stepFinish)
          stepsExtra.foreach(insert)
          updateStatesFrom(iStart)
          allowSupplyInjection = true
        }
      }
    }
  }

  private def extantAt(buildable: Buildable, step: MacroStep): Int = {
    val state = step.state
    buildable.asUnit.map(state.unitsExtant.apply)
      .orElse(buildable.asTech.map(t => Maff.fromBoolean(state.techs.contains(t))))
      .getOrElse(state.upgrades(buildable.asInstanceOf[Upgrade]))
  }

  private def extantBy(buildable: Buildable, absoluteFrame: Int = 0): Int = {
    extantAt(
      buildable,
      ?(absoluteFrame == 0,
        steps.last,
        steps
          .reverseIterator
          .find(_.event.dFrames + With.frame < absoluteFrame)
          .getOrElse(steps.head)))
  }

  private def tryInsertAfter(request: RequestBuildable, step: MacroStep, i: Int): InsertionResult = {
    lazy val atFrame = {
      val mineralFrames = Math.max(0, Maff.nanToInfinity((request.mineralCost - step.state.minerals)  / _simIncomeMineralsPerFrame))
      val gasFrames     = Math.max(0, Maff.nanToInfinity((request.gasCost     - step.state.gas)       / _simIncomeGasPerFrame))
      step.event.dFrames + Math.max(mineralFrames, gasFrames)
    }
    if (minInsert.get(request).exists(_ > i)) {
      return PrecedesMinInsert
    } else if (request.mineralCost > Math.max(0, step.state.minerals) && _simIncomeMineralsPerFrame == 0) {
      return MissingMinerals
    // TODO: Restore this check once we update income per-state
    // else if (request.gasCost > Math.max(0, step.state.gas) && _simIncomeGasPerFrame == 0
    } else if (request.gasCost > Math.max(0, step.state.gas) && step.state.unitsComplete(Terran.Refinery) + step.state.unitsComplete(Protoss.Assimilator) + step.state.unitsComplete(Zerg.Extractor) == 0) {
      return MissingGas
    } else if (request.upgradeRequired.exists(u => step.state.upgrades(u._1) < u._2)) {
      return MissingUpgrade
    } else if (request.techRequired.exists(t => ! step.state.techs.contains(t))) {
      return MissingTech
    } else if (step.state.producers(request.producerRequired) < request.producersRequired) {
      return MissingProducer
    } else if (request.unitsRequired.exists(_.withMacroSubstitutes.map(step.state.unitsComplete).sum == 0)) {
      return MissingUnitRequirement
    }

    var j = i + 1
    lazy val justInTimeSupply = request.unit.exists(_.isFarm) && request.minStartFrame > 0
    while (j < steps.length) {
      val futureStep = steps(j)
      val state = futureStep.state
      if (request.producerRequired != UnitClasses.None && state.producers(request.producerRequired) < request.producersRequired && futureStep.event.dFrames < atFrame + request.buildFrames) {
        return StealsProducer
      } else if (request.mineralCost > Math.max(0, state.minerals) && ! (justInTimeSupply && futureStep.request.exists(_.unit.exists(_.supplyRequired > 0)))) {
        return StealsMinerals
      } else if (request.gasCost > Math.max(0, state.gas)) {
        return StealsGas
      } else if (request.addonRequired.exists(addon => step.state.producers(AddonSubstitution.fromReal(addon)) <= 0)) {
        return StealsAddon
      }
      j += 1
    }

    // Do second loop checking for supply-stealing
    // We must do this AFTER the other checks in order to not say "this totally unaffordable request is fine because we're rejecting it for StealsSupply and can just add more supply"
    if ( ! _autosupply) { // Disabling this under the assumption that we will autosupply as needed
      j = i + 1
      while (j < steps.length) {
        val futureStep = steps(j)
        val state = futureStep.state
        if (request.supplyRequired > Math.max(0, state.supplyAvailable - state.supplyUsed)) {
          return StealsSupply
        }
        j += 1
      }
    }

    // Verify that the request doesn't block us on supply.
    // This check must happen last! We only want to attempt to clear a supply block if no other problems exist.
    //
    // Dilemma:
    // If we're trying to pump High Templar, followed by Zealots, are nearly maxed, and are low on gas,
    // then we may be reserving a huge buffer of supply for the High Templar that will go unused for a long time.
    // In the meantime, we could be making Zealots!
    //
    // So if an insertion would block us on supply, but put us near the supply cap, allow it.
    if ( ! _autosupply && request.supplyRequired > Math.max(0, step.state.supplyAvailable - step.state.supplyUsed) && step.state.supplyUsed < 380) {
      return MissingSupply
    }
    Success
  }

  private def fit(dFrames: Int): Int = {
    // Binary search to figure out where the step falls
    var min = 1
    var max = steps.length
    while(true) {
      val at = (min + max) / 2
      val dFramesBefore = if (at <= 0)            Int.MinValue else steps(at - 1).event.dFrames
      val dFramesAfter  = if (at >= steps.length) Int.MaxValue else steps(at    ).event.dFrames
      if (dFramesBefore > dFrames) {
        max = at - 1
      } else if (dFramesAfter <= dFrames) {
        min = at + 1
      } else {
        return at
      }
    }
    throw new RuntimeException("Failed to insert a MacroStep")
  }

  private def insert(step: MacroStep): Int = {
    val at = fit(step.event.dFrames)
    steps.insert(at, step)
    at
  }

  val consumingProducers: Array[UnitClass] = Array(Protoss.HighTemplar, Protoss.DarkTemplar, Zerg.Larva, Zerg.Hydralisk, Zerg.Hatchery, Zerg.Lair, Zerg.CreepColony, Zerg.Spire)
  private def constructStepsForRequest(request: RequestBuildable, framesAfter: Int): (MacroStep, MacroStep, Seq[MacroStep]) = {
    val consumeProducer     =   request.unit.isDefined && consumingProducers.contains(request.producerRequired)
    val stepStart           =   new MacroStep
    val stepFinish          =   new MacroStep
    val eventStart          =   stepStart.event
    val eventFinish         =   stepFinish.event
    stepStart.request       =   Some(request)
    eventStart.dFrames      =   framesAfter
    eventStart.dMinerals    = - request.mineralCost
    eventStart.dGas         = - request.gasCost
    eventStart.dSupplyUsed  =   request.supplyRequired
    eventStart.dProducer1   =   request.producerRequired
    eventStart.dProducer1N  = - request.producersRequired
    eventStart.dAddon       =   request.addonRequired.map(AddonSubstitution.fromReal).getOrElse(UnitClasses.None)
    eventStart.dAddonN      = - request.addonRequired.size
    eventFinish.dFrames     =   eventStart.dFrames + request.buildFrames
    if ( ! consumeProducer) {
      eventFinish.dProducer1  =   eventStart.dProducer1
      eventFinish.dProducer1N = - eventStart.dProducer1N
    }
    val ASAP = request.unit.exists(u => request.minStartFrame <= With.frame + Maff.div4(u.buildFrames))
    request.unit.foreach(u => {
      eventStart.dUnitExtant1         = u
      eventStart.dUnitExtant1N        = u.copiesProduced
      eventFinish.dUnitComplete       = u
      eventFinish.dUnitCompleteN      = u.copiesProduced
      eventFinish.dUnitCompleteASAP   = ?(ASAP, u, UnitClasses.None)
      eventFinish.dUnitCompleteASAPN  = ?(ASAP, u.copiesProduced, 0)
      eventFinish.dProducer2          = u
      eventFinish.dProducer2N         = 1
      eventFinish.dAddon              = request.addonRequired.map(AddonSubstitution.fromReal).getOrElse(?(AddonSubstitution(u), AddonSubstitution.fromReal(u), UnitClasses.None))
      eventFinish.dAddonN             = ?(AddonSubstitution(u), 1, request.addonRequired.size)
      eventFinish.dSupplyAvailable    = u.supplyProvided
      eventFinish.dGeysers            = Maff.fromBoolean(u.isGas)
    })
    if (Seq(Protoss.HighTemplar, Protoss.DarkTemplar, Zerg.Larva, Zerg.Drone, Zerg.Hatchery, Zerg.Lair, Zerg.CreepColony, Zerg.Spire, Zerg.Hydralisk, Zerg.Mutalisk).contains(request.producerRequired)) {
      eventStart.dUnitExtant2   =   request.producerRequired
      eventStart.dUnitExtant2N  = - request.producersRequired
    }
    request.tech.foreach(eventFinish.dTech = _)
    request.upgrade.foreach(upgrade => {
      eventFinish.dUpgrade      = upgrade
      eventFinish.dUpgradeLevel = request.quantity
    })
    val stepsExtra = ?(eventFinish.dUnitComplete.isHatchlike, larvaSteps(stepFinish.event.dFrames, ASAP), Seq.empty)
    (stepStart, stepFinish, stepsExtra)
  }

  private def larvaSteps(startFrame: Int, ASAP: Boolean): Seq[MacroStep] = {
    (0 until 12).map(i => {
      val larvaStep = new MacroStep
      val larvaEvent = larvaStep.event
      larvaEvent.dFrames            = startFrame + 342 * i
      larvaEvent.dUnitExtant1       = Zerg.Larva
      larvaEvent.dUnitExtant1N      = 1
      larvaEvent.dUnitComplete      = Zerg.Larva
      larvaEvent.dUnitCompleteN     = 1
      larvaEvent.dUnitCompleteASAP  = ?(ASAP, Zerg.Larva, UnitClasses.None)
      larvaEvent.dUnitCompleteASAPN = ?(ASAP, 1, 0)
      larvaEvent.dProducer1         = Zerg.Larva
      larvaEvent.dProducer1N        = 1
      larvaStep
    })
  }

  lazy val suppliers: Vector[UnitClass] = UnitClasses.all.filter(_.supplyProvided > 0)
  def ourSuppliers: Seq[UnitClass]  = suppliers.view.filter(_.race == With.self.raceCurrent) // Exclude the town halls for now due to how long they take to finish
  private def updateStatesFrom(index: Int): Unit = {
    var i = index
    while (i < steps.length) {
      val stateLast = steps(i - 1).state
      val stateNext = steps(i).state
      val event     = steps(i).event
      val dFrames   = event.dFrames - steps(i - 1).event.dFrames
      stateNext.minerals          = stateLast.minerals        + event.dMinerals + (dFrames * _simIncomeMineralsPerFrame).toInt
      stateNext.gas               = stateLast.gas             + event.dGas      + (dFrames * _simIncomeGasPerFrame).toInt
      stateNext.supplyAvailable   = Math.min(400, stateLast.supplyAvailable + event.dSupplyAvailable)
      stateNext.supplyUsed        = stateLast.supplyUsed      + event.dSupplyUsed
      stateNext.mineralPatches    = stateLast.mineralPatches  + event.dMineralPatches
      stateNext.geysers           = stateLast.geysers         + event.dGeysers
      stateNext.techs             = stateLast.techs
      stateNext.upgrades          = stateLast.upgrades
      stateNext.unitsExtant       = stateLast.unitsExtant
      stateNext.unitsComplete     = stateLast.unitsComplete
      stateNext.unitsCompleteASAP = stateLast.unitsCompleteASAP
      stateNext.producers         = stateLast.producers
      if (event.dUpgrade          != Upgrades.None)                                                                                 stateNext.upgrades          = stateNext.upgrades.clone
      if (event.dUnitExtant1      != UnitClasses.None || event.dUnitExtant2 != UnitClasses.None)                                    stateNext.unitsExtant       = stateNext.unitsExtant.clone
      if (event.dUnitComplete     != UnitClasses.None)                                                                              stateNext.unitsComplete     = stateNext.unitsComplete.clone
      if (event.dUnitCompleteASAP != UnitClasses.None)                                                                              stateNext.unitsCompleteASAP = stateNext.unitsCompleteASAP.clone
      if (event.dProducer1        != UnitClasses.None || event.dProducer2 != UnitClasses.None || event.dAddon != UnitClasses.None)  stateNext.producers         = stateNext.producers.clone
      if (event.dTech             != Techs.None)        stateNext.techs                                       += event.dTech
      if (event.dUpgrade          != Upgrades.None)     stateNext.upgrades(event.dUpgrade)                    =  event.dUpgradeLevel
      if (event.dUnitExtant1      != UnitClasses.None)  stateNext.unitsExtant(event.dUnitExtant1)             += event.dUnitExtant1N
      if (event.dUnitExtant2      != UnitClasses.None)  stateNext.unitsExtant(event.dUnitExtant2)             += event.dUnitExtant2N
      if (event.dUnitComplete     != UnitClasses.None)  stateNext.unitsComplete(event.dUnitComplete)          += event.dUnitCompleteN
      if (event.dUnitCompleteASAP != UnitClasses.None)  stateNext.unitsCompleteASAP(event.dUnitCompleteASAP)  += event.dUnitCompleteASAPN
      if (event.dProducer1        != UnitClasses.None)  stateNext.producers(event.dProducer1)                 += event.dProducer1N
      if (event.dProducer2        != UnitClasses.None)  stateNext.producers(event.dProducer2)                 += event.dProducer2N
      if (event.dAddon            != UnitClasses.None)  stateNext.producers(event.dAddon)                     += event.dAddonN
      stateNext.supplyUsePerFrame = MacroProducers.ours.view.map(p => stateNext.unitsExtant(p.producer) * p.supplyUsePerFrame).sum
      i += 1
    }
  }
}
