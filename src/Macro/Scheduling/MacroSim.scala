package Macro.Scheduling

import Lifecycle.With
import Macro.Requests.{RequestBuildable, RequestTech, RequestUnit, RequestUpgrade}
import Mathematics.Maff
import ProxyBwapi.Buildable
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.Techs
import ProxyBwapi.UnitClasses.UnitClasses
import ProxyBwapi.UnitInfo.UnitInfo
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}
import Utilities.?
import Utilities.Time.Forever

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

final class MacroSim {
  val redundant = new ArrayBuffer[RequestBuildable]() // We don't need this for bot operation; it's just here for debugging
  val denied    = new ArrayBuffer[RequestBuildable]() // We don't need this for bot operation; it's just here for debugging
  val steps     = new mutable.ArrayBuffer[MacroStep]()
  val minInsert = new mutable.OpenHashMap[RequestBuildable, Int]()

  private def requests: Seq[(RequestBuildable, Int)] = steps.view.filter(_.request.isDefined).map(s => (s.request.get, s.event.dFrames))
  def queue: Seq[(RequestBuildable, Int)] = requests.filter(_._1.specificUnit.isDefined) ++ requests.filter(_._1.specificUnit.isEmpty)

  private def trulyUnoccupied(unit: UnitInfo): Boolean = unit.complete && (unit.remainingOccupationFrames == 0 || unit.isAny(Protoss.Reaver, Protoss.Carrier))
  def simulate(): Unit = {
    val requests = new ArrayBuffer[RequestBuildable]
    redundant.clear()
    denied.clear()
    steps.clear()
    minInsert.clear()

    // Construct initial state
    val initialStep = new MacroStep
    val initialState = initialStep.state
    initialState.minerals           = With.self.minerals
    initialState.gas                = With.self.gas
    initialState.supplyAvailable    = Math.min(400, With.units.ours.filter(u => u.complete || u.isAny(Zerg.Lair, Zerg.Hive)).map(_.unitClass.supplyProvided).sum)
    initialState.supplyUsed         = With.units.ours.map(_.unitClass.supplyRequired).sum
    initialState.mineralPatches     = With.self.bases.view.map(_.minerals.count(_.mineralsLeft >= 8)).sum
    initialState.geysers            = With.self.bases.view.map(_.gas.count(g => g.isOurs && g.complete && g.gasLeft > 0)).sum
    initialState.unitsExtant        = With.macroCounts.oursExtant
    initialState.unitsComplete      = With.macroCounts.oursComplete
    initialState.unitsCompleteASAP  = With.macroCounts.oursComplete
    Upgrades.all.foreach(u => initialState.upgrades(u) = With.self.getUpgradeLevel(u))
    initialState.techs ++= Techs.all.view.filter(With.self.hasTech)
    insert(initialStep)

    // Construct events for production in progress
    // TODO: Count Eggs/Cocoon/Lurker Egg as what they're making
    // TODO: Don't occupy Probes
    // TODO: Don't replace morphers
    With.units.ours.filter(trulyUnoccupied).foreach(u => initialState.producers(u.unitClass) += 1)
    With.units.ours.filterNot(trulyUnoccupied).foreach(u => {
      val step = new MacroStep
      val event = step.event
      event.dFrames = u.remainingOccupationFrames
      if ( ! u.complete) {
        step.request = Some(RequestUnit(u.unitClass, initialState.unitsExtant(u.unitClass), specificUnitArg = Some(u)))
        event.dUnitComplete       = u.unitClass
        event.dUnitCompleteN      = 1
        event.dUnitCompleteASAP   = u.unitClass
        event.dUnitCompleteASAPN  = 1
        if ( ! u.isAny(Zerg.Lair, Zerg.Hive)) {
          event.dSupplyAvailable += u.unitClass.supplyProvided
        }
      }
      if (u.upgrading) {
        step.request = Some(RequestUpgrade(u.upgradingType, 1 + With.self.getUpgradeLevel(u.upgradingType)))
        event.dUpgrade = u.upgradingType
        event.dUpgradeLevel = 1 + initialState.upgrades(u.upgradingType)
      } else if (u.teching) {
        step.request = Some(RequestTech(u.techingType))
        event.dTech = u.techingType
      } else if (u.unitClass.isGas) {
        event.dGeysers += 1
      } else if ( ! u.complete && u.unitClass.isResourceDepot && ! u.isAny(Zerg.Lair, Zerg.Hive)) {
        val base = u.base.filter(_.townHall.contains(u))
        base.foreach(b => event.dMineralPatches += b.minerals.count(_.mineralsLeft >= 8))
      }
      event.dProducer1 = u.unitClass
      event.dProducer1N = 1
      insert(step)
    })

    // Populate states as of each event
    updateStatesFrom(1)

    // Insert requests
    With.scheduler.requests.view
      .flatMap(_._2)
      .filterNot(r => With.blackboard.toCancel().contains(r.buildable))
      .foreach(buildRequest => {
        if (buildRequest.upgrade.exists(u => steps.last.state.upgrades(u) < buildRequest.quantity)) {
          requests += buildRequest
        } else if (buildRequest.tech.exists(t => ! steps.last.state.techs.contains(t))) {
          requests += buildRequest
        } else if (buildRequest.unit.isDefined) {
          requests += buildRequest
        }
      })

    // For each request, check if we've satisfied it by the end, and if not, insert it
    requests.foreach(request => {
      // TODO: Don't occupy Probes
      // TODO: Don't return producers which morph
      // TODO: Reduce diff for zergling/scourge

      // By the end of our simulation, have we not yet met the request?
      //
      // Units:
      // - (Default) If there is no placement query, use our state count
      // - (Special) If there is a  placement query, count our complete units which the tile filter accepts

      val unitDiff = request.unit.map(u => request.quantity -
        (if (request.placement.isEmpty)
          (if (request.minStartFrame <= 0) steps.last.state.unitsCompleteASAP(u) else extantBy(request.buildable, request.minStartFrame))
        else
          With.units.ours.filter(u).map(_.tileTopLeft).count(request.placement.get.acceptExisting))).getOrElse(0)
      val upgradeDiff = request.upgrade.map(u => request.quantity - steps.last.state.upgrades(u)).getOrElse(0)
      val techDiff    = Maff.fromBoolean(request.tech.exists(t => ! steps.last.state.techs.contains(t)))
      val diff        = Seq(unitDiff, upgradeDiff, techDiff).max
      if (diff <= 0) {
        redundant += request
      } else {
        (0 until diff).foreach(iDiff => {
          val insertAfter = steps.indices.drop(minInsert.getOrElse(request, 0)).find(canInsertAfter(request, _))
          if (insertAfter.isEmpty) { denied += request }
          insertAfter.foreach(i => {
            val stepBefore = steps(i)
            val framesAfter = Math.ceil(Seq(
              request.minStartFrame - With.frame,
              stepBefore.event.dFrames,
              if (request.mineralCost == 0) 0 else stepBefore.event.dFrames + Maff.nanToN((request.mineralCost  - stepBefore.state.minerals)  / With.accounting.ourIncomePerFrameMinerals,  Forever()),
              if (request.gasCost     == 0) 0 else stepBefore.event.dFrames + Maff.nanToN((request.gasCost      - stepBefore.state.gas)       / With.accounting.ourIncomePerFrameGas,       Forever()))
              .max).toInt
            if (framesAfter < Forever()) {
              val stepStart           = new MacroStep
              val stepFinish          = new MacroStep
              val eventStart          = stepStart.event
              val eventFinish         = stepFinish.event
              stepStart.request       = Some(request)
              eventStart.dFrames      = framesAfter
              eventStart.dMinerals    = - request.mineralCost
              eventStart.dGas         = - request.gasCost
              eventStart.dSupplyUsed  = request.supplyRequired
              eventStart.dProducer1   = request.producerRequired
              eventStart.dProducer1N  = - request.producersRequired
              eventFinish.dFrames     = eventStart.dFrames + request.buildFrames
              eventFinish.dProducer1  = eventStart.dProducer1
              eventFinish.dProducer1N = - eventStart.dProducer1N
              request.unit.foreach(u => {
                val ASAP                        = request.minStartFrame <= With.frame + u.buildFrames / 4
                eventStart.dUnitExtant1         = u
                eventStart.dUnitExtant1N        = u.copiesProduced
                eventFinish.dUnitComplete       = u
                eventFinish.dUnitCompleteN      = u.copiesProduced
                eventFinish.dUnitCompleteASAP   = ?(ASAP, u, UnitClasses.None)
                eventFinish.dUnitCompleteASAPN  = ?(ASAP, u.copiesProduced, 0)
                eventFinish.dProducer2          = u
                eventFinish.dProducer2N         = u.copiesProduced
                eventFinish.dSupplyAvailable    = u.supplyProvided
                eventFinish.dGeysers            = Maff.fromBoolean(u.isGas)
              })
              if (Seq(Protoss.HighTemplar, Protoss.DarkTemplar, Zerg.Larva, Zerg.Drone, Zerg.CreepColony, Zerg.Hydralisk, Zerg.Mutalisk).contains(request.producerRequired)) {
                eventStart.dUnitExtant2 = request.producerRequired
                eventStart.dUnitExtant2N = - request.producersRequired
              }
              request.tech.foreach(eventFinish.dTech = _)
              request.upgrade.foreach(upgrade => {
                eventFinish.dUpgrade = upgrade
                eventFinish.dUpgradeLevel = request.quantity
              })
              val iStart = insert(stepStart)
              insert(stepFinish)
              updateStatesFrom(iStart)
            }
          })
        })
      }
    })
  }

  private def extantAt(buildable: Buildable, step: MacroStep): Int = {
    val state = step.state
    buildable.asUnit.map(state.unitsExtant.apply)
      .orElse(buildable.asTech.map(t => Maff.fromBoolean(state.techs.contains(t))))
      .getOrElse(state.upgrades(buildable.asInstanceOf[Upgrade]))
  }

  private def extantBy(buildable: Buildable, absoluteFrame: Int = 0): Int = {
    extantAt(buildable, if (absoluteFrame == 0) steps.last else steps.reverseIterator.find(_.event.dFrames + With.frame < absoluteFrame).getOrElse(steps.head))
  }

  private def canInsertAfter(request: RequestBuildable, i: Int): Boolean = {
    // Find a state where we can fulfill the request
    val step = steps(i)
    var cant = false
    lazy val atFrame = {
      val mineralFrames = Math.max(0, Maff.nanToInfinity((request.mineralCost - step.state.minerals)  / With.accounting.ourIncomePerFrameMinerals))
      val gasFrames     = Math.max(0, Maff.nanToInfinity((request.gasCost     - step.state.gas)       / With.accounting.ourIncomePerFrameGas))
      step.event.dFrames + Math.max(mineralFrames, gasFrames)
    }
    cant ||= exceedsMinInsert(request, i + 1)
    cant ||= request.mineralCost > Math.max(0, step.state.minerals) && With.accounting.ourIncomePerFrameMinerals == 0
    // TODO: Restore once we update income per-state
    //cant ||= request.gasCost > Math.max(0, step.state.gas) && With.accounting.ourIncomePerFrameGas == 0
    cant ||= request.gasCost > Math.max(0, step.state.gas) && step.state.unitsComplete(Terran.Refinery) + step.state.unitsComplete(Protoss.Assimilator) + step.state.unitsComplete(Zerg.Extractor) == 0
    cant ||= request.supplyRequired > Math.max(0, step.state.supplyAvailable - step.state.supplyUsed)
    cant ||= request.upgradeRequired.exists(u => step.state.upgrades(u._1) < u._2)
    cant ||= request.techRequired.exists(t => ! step.state.techs.contains(t))
    cant ||= step.state.producers(request.producerRequired) < request.producersRequired
    cant ||= request.unitsRequired.exists(_.withMacroSubstitutes.map(step.state.unitsComplete).sum == 0)
    cant ||= ! canInsertBefore(request, i + 1, atFrame.toInt)
    // TODO: Treat addons as producers
    if (cant) {
      minInsert(request) = Math.max(i, minInsert.getOrElse(request, i))
    }
    ! cant
  }

  private def canInsertBefore(request: RequestBuildable, i: Int, atFrame: Int): Boolean = {
    // Ensure no future states would be rendered impossible by inserting the request
    var j = i
    var cant = false
    while(j < steps.length && ! cant) {
      val step = steps(j)
      val state = step.state
      cant ||= request.producerRequired != UnitClasses.None && state.producers(request.producerRequired) < request.producersRequired && step.event.dFrames < atFrame + request.buildFrames
      cant ||= request.mineralCost      > Math.max(0, state.minerals)
      cant ||= request.gasCost          > Math.max(0, state.gas)
      cant ||= request.supplyRequired   > Math.max(0, state.supplyAvailable - state.supplyUsed)
      j += 1
    }
    ! cant
  }

  private def exceedsMinInsert(request: RequestBuildable, i: Int): Boolean = {
    minInsert.get(request).exists(_ >= i)
  }

  private def insert(step: MacroStep): Int = {
    // Binary search to figure out where to fit the step
    val dFrames = step.event.dFrames
    var min = 1
    var max = steps.length
    while(true) {
      val at = (min + max) / 2
      val dFramesBefore = if (at <= 0) Int.MinValue else steps(at - 1).event.dFrames
      val dFramesAfter = if (at >= steps.length) Int.MaxValue else steps(at).event.dFrames
      if (dFramesBefore > dFrames) {
        max = at - 1
      } else if (dFramesAfter <= dFrames) {
        min = at + 1
      } else {
        steps.insert(at, step)
        return at
      }
    }
    throw new RuntimeException("Failed to insert a MacroStep")
  }

  private def updateStatesFrom(index: Int): Unit = {
    var i = index
    while (i < steps.length) {
      val stateLast = steps(i - 1).state
      val stateNext = steps(i).state
      val event     = steps(i).event
      val dFrames   = event.dFrames - steps(i - 1).event.dFrames
      stateNext.minerals          = stateLast.minerals + event.dMinerals + (dFrames * With.accounting.ourIncomePerFrameMinerals).toInt
      stateNext.gas               = stateLast.gas + event.dGas + (dFrames * With.accounting.ourIncomePerFrameGas).toInt
      stateNext.supplyAvailable   = Math.min(400, stateLast.supplyAvailable + event.dSupplyAvailable)
      stateNext.supplyUsed        = stateLast.supplyUsed + event.dSupplyUsed
      stateNext.mineralPatches    = stateLast.mineralPatches + event.dMineralPatches
      stateNext.geysers           = stateLast.geysers + event.dGeysers
      stateNext.techs             = stateLast.techs
      stateNext.upgrades          = stateLast.upgrades
      stateNext.unitsExtant       = stateLast.unitsExtant
      stateNext.unitsComplete     = stateLast.unitsComplete
      stateNext.unitsCompleteASAP = stateLast.unitsCompleteASAP
      stateNext.producers         = stateLast.producers
      if (event.dUpgrade          != Upgrades.None)                                               stateNext.upgrades          = stateNext.upgrades.clone
      if (event.dUnitExtant1      != UnitClasses.None || event.dUnitExtant2 != UnitClasses.None)  stateNext.unitsExtant       = stateNext.unitsExtant.clone
      if (event.dUnitComplete     != UnitClasses.None)                                            stateNext.unitsComplete     = stateNext.unitsComplete.clone
      if (event.dUnitCompleteASAP != UnitClasses.None)                                            stateNext.unitsCompleteASAP = stateNext.unitsCompleteASAP.clone
      if (event.dProducer1        != UnitClasses.None || event.dProducer2 != UnitClasses.None)    stateNext.producers         = stateNext.producers.clone
      if (event.dTech             != Techs.None)        stateNext.techs                                       += event.dTech
      if (event.dUpgrade          != Upgrades.None)     stateNext.upgrades(event.dUpgrade)                    =  event.dUpgradeLevel
      if (event.dUnitExtant1      != UnitClasses.None)  stateNext.unitsExtant(event.dUnitExtant1)             += event.dUnitExtant1N
      if (event.dUnitExtant2      != UnitClasses.None)  stateNext.unitsExtant(event.dUnitExtant2)             += event.dUnitExtant2N
      if (event.dUnitComplete     != UnitClasses.None)  stateNext.unitsComplete(event.dUnitComplete)          += event.dUnitCompleteN
      if (event.dUnitCompleteASAP != UnitClasses.None)  stateNext.unitsCompleteASAP(event.dUnitCompleteASAP)  += event.dUnitCompleteASAPN
      if (event.dProducer1        != UnitClasses.None)  stateNext.producers(event.dProducer1)                 += event.dProducer1N
      if (event.dProducer2        != UnitClasses.None)  stateNext.producers(event.dProducer2)                 += event.dProducer2N
      i += 1
    }
  }
}
