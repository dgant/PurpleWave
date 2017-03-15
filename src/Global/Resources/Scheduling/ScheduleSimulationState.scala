package Global.Resources.Scheduling

import Startup.With
import Types.Buildable.{Buildable, BuildableUnit}
import Utilities.CountMap
import bwapi.{TechType, UnitType, UpgradeType}

import scala.collection.mutable

class ScheduleSimulationState(
                               var frame               : Int,
                               var minerals            : Int,
                               var gas                 : Int,
                               var supplyAvailable     : Int,
                               var unitsOwned          : CountMap[UnitType],
                               var unitsAvailable      : CountMap[UnitType],
                               var techsOwned          : mutable.Set[TechType],
                               var upgradeLevels       : mutable.Map[UpgradeType, Int],
                               val eventQueue          : mutable.SortedSet[BuildEvent],
                               val isDisposableCopy    : Boolean = false) {
  
  private def disposableCopy:ScheduleSimulationState =
    new ScheduleSimulationState(
      frame,
      minerals,
      gas,
      supplyAvailable,
      unitsOwned.clone,
      unitsAvailable.clone,
      techsOwned.clone,
      upgradeLevels.clone,
      eventQueue.clone,
      isDisposableCopy = true)
  
  //////////////////////
  // Public interface //
  //////////////////////
  
  def assumeEvent(event:BuildEvent) {
    eventQueue.add(event)
    
    if      (event.frameStart <  frame) reserveBuilders(event.buildable)
    else if (event.frameStart == frame) startEvent(event)
  }
  
  def tryBuilding(buildable: Buildable, maxFrames:Int): TryBuildingResult = {
    if (isBuildableNow(buildable)) {
      val event = new BuildEvent(buildable, frame, frame + buildable.frames)
      val futureWithThisEvent = disposableCopy
      futureWithThisEvent.eventQueue.add(event)
      if (futureWithThisEvent.allEventsStillBuildableOnTime) {
        return new TryBuildingResult(Some(event))
      }
    }
  
    val nextFrame = nextInterestingFrame(Some(buildable))
    if (nextFrame > maxFrames) {
      return new TryBuildingResult(None, exceededSearchDepth = true)
    }
    
    val nextState = if (isDisposableCopy) this else disposableCopy
    nextState.tryFastForward(nextFrame)
    nextState.tryBuilding(buildable, maxFrames)
  }
  
  ///////////////////////////////////
  // Features of the current state //
  ///////////////////////////////////
  
  private val never = Int.MaxValue
  
  private def isBuildableNow(buildable: Buildable):Boolean = {
    framesBeforeMinerals(buildable) == 0 &&
    framesBeforeGas(buildable)      == 0 &&
    unmetPrerequisites(buildable).isEmpty
  }
  
  private def framesBeforeMinerals(buildable: Buildable):Int = framesBeforeResource(
    buildable,
    current     = minerals,
    needed      = buildable.minerals,
    rate        = mineralsPerFrame)
  
  private def framesBeforeGas(buildable: Buildable):Int = framesBeforeResource(
    buildable,
    current     = gas,
    needed      = buildable.gas,
    rate        = gasPerFrame)
  
  private def framesBeforeResource(
    buildable:  Buildable,
    current:    Int,
    needed:     Int,
    rate:       Double):Int = {
    if (current >= needed)               return 0
    if (current <  needed && rate <= 0)  return never
    Math.max(0, Math.ceil((needed - current)/rate)).toInt
  }
  
  private def unmetPrerequisites(buildable: Buildable): Iterable[Buildable] = {
    unmetSupply(buildable) ++ unmetRequirements(buildable) ++ unmetBuilders(buildable)
  }
  
  private def unmetSupply(buildable: Buildable): Iterable[Buildable] = {
    val supplyType = With.self.getRace.getSupplyProvider
    (0 until Math.max(0, (buildable.supplyRequired - supplyAvailable) / supplyType.supplyProvided))
      .map(i => new BuildableUnit(supplyType))
  }
  
  private def unmetRequirements(buildable: Buildable): Iterable[Buildable] = {
    val units  = new CountMap[UnitType]
    buildable.requirements
      .map(requirement => {
        requirement.unitOption.foreach(units.addOne)
        var unmet = false
        unmet ||= requirement.techOption    .exists(tech    => ! techsOwned.contains(tech))
        unmet ||= requirement.upgradeOption .exists(upgrade => upgradeLevels.getOrElse(upgrade, 0)  < requirement.upgradeLevel)
        unmet ||= requirement.unitOption    .exists(unit    => owned(unit) < units(unit))
        if (unmet) Some(requirement) else None
      })
      .filter(_.isDefined)
      .map(_.get)
  }
  
  private def unmetBuilders(buildable: Buildable): Iterable[Buildable] = {
    val buildersRequired  = new CountMap[UnitType]
    buildable.buildersOccupied
      .map(builder => {
        builder.unitOption.foreach(buildersRequired.addOne)
        if (builder.unitOption.exists(unit => available(unit) < buildersRequired(unit)))
          Some(builder) else None
      })
      .filter(_.isDefined)
      .map(_.get)
  }
  
  private def mineralsPerFrame : Double  = With.economy.mineralIncomePerMinute (numberOfMiners,   numberOfBases) / 24.0 / 60.0
  private def gasPerFrame      : Double  = With.economy.gasIncomePerMinute     (numberOfDrillers, numberOfBases) / 24.0 / 60.0
  
  private def owned     (unitType: UnitType) : Int = unitsOwned      .get(unitType).getOrElse(0)
  private def available (unitType: UnitType) : Int = unitsAvailable  .get(unitType).getOrElse(0)
  private def numberOfBases     : Int = List(UnitType.Terran_Command_Center, UnitType.Protoss_Nexus, UnitType.Zerg_Hatchery, UnitType.Zerg_Lair, UnitType.Zerg_Hive).map(owned).sum
  private def numberOfWorkers   : Int = List(UnitType.Terran_SCV, UnitType.Protoss_Probe, UnitType.Zerg_Drone).map(available).sum
  private def numberOfMiners    : Int = Math.max(0, numberOfWorkers - numberOfDrillers)
  private def numberOfDrillers  : Int = Math.min(
    3 * List(UnitType.Terran_Refinery, UnitType.Protoss_Assimilator, UnitType.Zerg_Extractor).map(available).sum,
    numberOfWorkers / 3)
  
  private def nextEventByStart : BuildEvent = eventQueue.minBy(_.frameStart)
  private def nextEventByEnd   : BuildEvent = eventQueue.minBy(_.frameEnd)
  
  ////////////////////////////
  // Running the simulation //
  ////////////////////////////
  
  private def isInTheFuture(someFrame: Int):Boolean = someFrame > frame
  
  private def allEventsStillBuildableOnTime:Boolean = {
    while(nextInterestingFrame() < never)
      if ( ! tryFastForward(nextInterestingFrame(), testIntegrity = true)) return false
    return true
  }
  
  private def nextInterestingFrame(buildable:Option[Buildable] = None):Int = {
    List(
      buildable.map(frame + framesBeforeMinerals (_)).getOrElse(never),
      buildable.map(frame + framesBeforeGas      (_)).getOrElse(never),
      (eventQueue.map(_.frameStart) .filter(isInTheFuture) ++ List(never)).min,
      (eventQueue.map(_.frameEnd)   .filter(isInTheFuture) ++ List(never)).min
    )
    .filter(isInTheFuture)
    .min
  }
  
  private def tryFastForward(nextFrame:Int, testIntegrity:Boolean = false):Boolean = {
    minerals  += ((nextFrame - frame) * mineralsPerFrame ).toInt
    gas       += ((nextFrame - frame) * gasPerFrame      ).toInt
    frame     = nextFrame
    
    var allEventsStillBuildable = true
    val eventsNowEnding = eventsEnding
    eventsNowEnding.foreach(endEvent)
    eventsNowEnding.foreach(eventQueue.remove)
    eventsStarting.foreach(event => {
      if (testIntegrity) allEventsStillBuildable &&= isBuildableNow(event.buildable)
      startEvent(event)
    })
    allEventsStillBuildable
  }
  
  private def eventsStarting : Iterable[BuildEvent] = eventQueue.filter(_.frameStart == frame)
  private def eventsEnding   : Iterable[BuildEvent] = eventQueue.filter(_.frameEnd   == frame)
  
  ///////////////////////////////////
  // Mutating the simulation state //
  ///////////////////////////////////
  
  private def startEvent(event: BuildEvent) {
    spendResources(event.buildable)
    reserveBuilders(event.buildable)
    spendBuilders(event.buildable)
  }
  
  private def endEvent(event: BuildEvent) {
    val buildable = event.buildable
    supplyAvailable += buildable.supplyProvided
    buildable.unitOption.foreach(unitType => unitsOwned.add(unitType, buildable.unitsProduced))
    buildable.unitOption.foreach(unitType => unitsAvailable.add(unitType, buildable.unitsProduced))
    buildable.buildersOccupied.map(_.unit).foreach(unitsAvailable.addOne)
    buildable.techOption.foreach(techsOwned.add)
    buildable.upgradeOption.foreach(upgradeLevels.put(_, buildable.upgradeLevel))
  }
  
  private def spendResources(buildable: Buildable) {
    minerals        -= buildable.minerals
    gas             -= buildable.gas
    supplyAvailable -= buildable.supplyRequired
  }
  
  private def reserveBuilders(buildable: Buildable) {
    //TODO: Try to account for travel time
    buildable.buildersOccupied.map(_.unit).foreach(unitsAvailable.subtractOne)
  }
  
  private def spendBuilders(buildable: Buildable) {
    buildable.buildersConsumed.map(_.unit).foreach(unitsOwned.subtractOne)
  }
}