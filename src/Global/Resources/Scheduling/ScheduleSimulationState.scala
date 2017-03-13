package Global.Resources.Scheduling

import Startup.With
import Types.Buildable.{Buildable, BuildableUnit}
import bwapi.{TechType, UnitType, UpgradeType}

import scala.collection.mutable

class ScheduleSimulationState(
  var frame               : Int,
  var minerals            : Int,
  var gas                 : Int,
  var supplyAvailable     : Int,
  var unitsOwned          : mutable.HashMap[UnitType, Int],
  var unitsAvailable      : mutable.HashMap[UnitType, Int],
  var techsOwned          : mutable.Set[TechType],
  var upgradeLevels       : mutable.Map[UpgradeType, Int],
  val eventQueue          : mutable.SortedSet[SimulationEvent],
  val isDisposableCopy    : Boolean = false) {

  def disposableCopy:ScheduleSimulationState =
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
  
  ///////////////////////////////////
  // Features of the current state //
  ///////////////////////////////////
  
  val never = Int.MaxValue
  
  def isBuildableNow(buildable: Buildable):Boolean = {
    framesBeforeMinerals(buildable) == 0 &&
    framesBeforeGas(buildable)      == 0 &&
    unmetPrerequisites(buildable).isEmpty
  }
  
  def framesBeforeMinerals(buildable: Buildable):Int = framesBeforeResource(
    buildable,
    current     = minerals,
    needed      = buildable.minerals,
    rate        = mineralsPerFrame)
  
  def framesBeforeGas(buildable: Buildable):Int = framesBeforeResource(
    buildable,
    current     = gas,
    needed      = buildable.gas,
    rate        = gasPerFrame)
  
  def framesBeforeResource(
    buildable:  Buildable,
    current:    Int,
    needed:     Int,
    rate:       Double):Int = {
    if (current >= needed)               return 0
    if (current <  needed && rate <= 0)  return never
    Math.max(0, Math.ceil((needed - current)/rate)).toInt
  }
  
  def unmetPrerequisites(buildable: Buildable): Iterable[Buildable] = {
    unmetSupply(buildable) ++ unmetRequirements(buildable) ++ unmetBuilders(buildable)
  }
  
  def unmetSupply(buildable: Buildable): Iterable[Buildable] = {
    val supplyType = With.game.self.getRace.getSupplyProvider
    (0 until Math.max(0, (buildable.supplyRequired - supplyAvailable) / supplyType.supplyProvided))
      .map(i => new BuildableUnit(supplyType))
  }
  
  def unmetRequirements(buildable: Buildable): Iterable[Buildable] = {
    val units  = new mutable.HashMap[UnitType, Int]
    buildable.requirements
      .map(requirement => {
        requirement.unitOption.foreach(unit => units.put(unit, 1 + units.getOrElse(unit, 0)))
        var unmet = false
        unmet ||= requirement.techOption    .exists(tech    => ! techsOwned.contains(tech))
        unmet ||= requirement.upgradeOption .exists(upgrade => upgradeLevels.getOrElse(upgrade, 0)  < requirement.upgradeLevel)
        unmet ||= requirement.unitOption    .exists(unit    => owned(unit) < units(unit))
        if (unmet) Some(requirement) else None
      })
      .filter(_.isDefined)
      .map(_.get)
  }
  
  def unmetBuilders(buildable: Buildable): Iterable[Buildable] = {
    val buildersRequired  = new mutable.HashMap[UnitType, Int]
    buildable.buildersOccupied
      .map(builder => {
        builder.unitOption.foreach(unit => buildersRequired.put(unit, 1 + buildersRequired.getOrElse(unit, 0)))
        if (builder.unitOption.exists(unit => available(unit) < buildersRequired(unit)))
          Some(builder) else None
      })
      .filter(_.isDefined)
      .map(_.get)
  }
  
  def mineralsPerFrame : Double  = With.economy.mineralIncomePerMinute (numberOfMiners,   numberOfBases) / 24.0 / 60.0
  def gasPerFrame      : Double  = With.economy.gasIncomePerMinute     (numberOfDrillers, numberOfBases) / 24.0 / 60.0
  
  def owned     (unitType: UnitType) : Int = unitsOwned      .get(unitType).getOrElse(0)
  def available (unitType: UnitType) : Int = unitsAvailable  .get(unitType).getOrElse(0)
  def numberOfBases     : Int = List(UnitType.Terran_Command_Center, UnitType.Protoss_Nexus, UnitType.Zerg_Hatchery, UnitType.Zerg_Lair, UnitType.Zerg_Hive).map(owned).sum
  def numberOfWorkers   : Int = List(UnitType.Terran_SCV, UnitType.Protoss_Probe, UnitType.Zerg_Drone).map(available).sum
  def numberOfMiners    : Int = Math.max(0, numberOfWorkers - numberOfDrillers)
  def numberOfDrillers  : Int = Math.min(
    3 * List(UnitType.Terran_Refinery, UnitType.Protoss_Assimilator, UnitType.Zerg_Extractor).map(available).sum,
    numberOfWorkers / 3)
  
  def nextEventByStart : SimulationEvent = eventQueue.minBy(_.frameStart)
  def nextEventByEnd   : SimulationEvent = eventQueue.minBy(_.frameEnd)
  
  ////////////////////////////
  // Running the simulation //
  ////////////////////////////
  
  def isInTheFuture(someFrame: Int):Boolean = someFrame > frame
  
  def simulateBuilding(buildable: Buildable, maxFrames:Int): ScheduleSimulationBuildResult = {
    if (isBuildableNow(buildable)) {
      val event = new SimulationEvent(buildable, frame, frame + buildable.frames)
      val futureWithThisEvent = disposableCopy
      futureWithThisEvent.eventQueue.add(event)
      if (futureWithThisEvent.allEventsStillBuildableOnTime) {
        return new ScheduleSimulationBuildResult(Some(event))
      }
    }
  
    val nextFrame = nextInterestingFrame(Some(buildable))
    if (nextFrame > maxFrames) return new ScheduleSimulationBuildResult(None, exceededSearchDepth = true)
    
    val nextState = if (isDisposableCopy) this else disposableCopy
    nextState.fastForward(nextFrame)
    nextState.simulateBuilding(buildable, maxFrames)
  }
  
  def allEventsStillBuildableOnTime:Boolean = {
    if ( ! eventsStarting.forall(event => isBuildableNow(event.buildable))) return false
    while(nextInterestingFrame() < never)
      if ( ! fastForward(nextInterestingFrame(), testIntegrity = true)) return false
    return true
  }
  
  def nextInterestingFrame(buildable:Option[Buildable] = None):Int = {
    List(
      buildable.map(frame + framesBeforeMinerals (_)).getOrElse(never),
      buildable.map(frame + framesBeforeGas      (_)).getOrElse(never),
      (eventQueue.map(_.frameStart) .filter(isInTheFuture) ++ List(never)).min,
      (eventQueue.map(_.frameEnd)   .filter(isInTheFuture) ++ List(never)).min
    )
    .filter(isInTheFuture)
    .min
  }
  
  def fastForward(nextFrame:Int, testIntegrity:Boolean = false):Boolean = {
    minerals  += ((nextFrame - frame) * mineralsPerFrame ).toInt
    gas       += ((nextFrame - frame) * gasPerFrame      ).toInt
    frame     = nextFrame
    
    var allEventsStillBuildable = true
    eventsStarting.foreach(event => {
      if (testIntegrity) allEventsStillBuildable &&= isBuildableNow(event.buildable)
      startEvent(event)
    })
    val eventsNowEnding = eventsEnding
    eventsNowEnding.foreach(endEvent)
    eventsNowEnding.foreach(eventQueue.remove)
    
    allEventsStillBuildable
  }
  
  def eventsStarting : Iterable[SimulationEvent] = eventQueue.filter(_.frameStart == frame)
  def eventsEnding   : Iterable[SimulationEvent] = eventQueue.filter(_.frameEnd   == frame)
  
  ///////////////////////////////////
  // Mutating the simulation state //
  ///////////////////////////////////
  
  def startInitialEvent(event:SimulationEvent) {
    reserveBuilders(event.buildable)
  }
  
  def startEvent(event: SimulationEvent) {
    spendResources(event.buildable)
    reserveBuilders(event.buildable)
    spendBuilders(event.buildable)
  }
  
  def endEvent(event: SimulationEvent) {
    val buildable = event.buildable
    supplyAvailable += buildable.supplyProvided
    if ( ! event.describesFreedomOfExistingUnit) buildable.unitOption.foreach(addOwnedUnit)
    buildable.unitOption.foreach(addAvailableUnit)
    buildable.techOption.foreach(addTech)
    buildable.upgradeOption.foreach(addUpgrade(_, buildable.upgradeLevel))
  }
  
  def spendResources(buildable: Buildable) {
    minerals        -= buildable.minerals
    gas             -= buildable.gas
    supplyAvailable -= buildable.supplyRequired
  }
  
  def reserveBuilders(buildable: Buildable) {
    //TODO: Don't add builder back if it's consumed
    //TODO: Try to account for travel time
    buildable.buildersOccupied.foreach(builder => {
      unitsAvailable.put(builder.unit, -1 + unitsAvailable.getOrElse(builder.unit, 0))
      eventQueue.add(new SimulationEvent(builder, frame, frame + buildable.frames, describesFreedomOfExistingUnit = true))
    })
  }
  
  def spendBuilders(buildable: Buildable) {
    buildable.buildersConsumed.foreach(builder => unitsOwned(builder.unit) -= 1)
  }
  
  def addOwnedUnit(unitType: UnitType)                = unitsOwned.put     (unitType, 1 + owned(unitType))
  def addAvailableUnit(unitType: UnitType)            = unitsAvailable.put (unitType, 1 + available(unitType))
  def addTech(techType: TechType)                     = techsOwned.add     (techType)
  def addUpgrade(upgradeType: UpgradeType, level:Int) = upgradeLevels.put  (upgradeType, level)
}